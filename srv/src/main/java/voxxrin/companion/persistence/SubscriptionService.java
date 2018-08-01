package voxxrin.companion.persistence;

import org.joda.time.DateTime;
import restx.WebException;
import restx.http.HttpStatus;
import restx.jongo.JongoCollection;
import voxxrin.companion.auth.AuthModule;
import voxxrin.companion.domain.*;
import voxxrin.companion.domain.technical.Reference;
import voxxrin.companion.utils.PresentationRef;

public abstract class SubscriptionService {

    private final JongoCollection subscriptionCollection;
    private final JongoCollection presentationCollection;

    public SubscriptionService(JongoCollection subscriptionCollection, JongoCollection presentationCollection) {
        this.subscriptionCollection = subscriptionCollection;
        this.presentationCollection = presentationCollection;
    }

    /**
     * Read
     */

    protected boolean isSubscribed(User user, Presentation presentation) {
        return subscriptionCollection
                .get()
                .count("{ presentationRef: #, userId: # }", PresentationRef.buildPresentationBusinessRef(presentation), user.getId()) > 0;
    }

    protected Iterable<Subscription> getSubscriptions(Presentation presentation) {
        return subscriptionCollection.get()
                .find("{ presentationRef: # }", PresentationRef.buildPresentationBusinessRef(presentation))
                .as(Subscription.class);
    }

    protected Iterable<Subscription> getSubscriptions(Event event) {
        return subscriptionCollection.get()
                .find("{ presentationRef: { $regex: # } }", String.format("%s:.*", event.getEventId()))
                .as(Subscription.class);
    }

    protected Iterable<EventPresentations> findSubscribedPresentations(User user) {
        return presentationCollection.get()
                .aggregate("{ " +
                                "   $match: { " +
                                "       externalId: { $exists: true }, " +
                                "       eventId: { $exists: true }," +
                                "       from: { $gte: # } " +
                                "   } " +
                                "}",
                        DateTime.now().toDate()
                )
                .and("{ " +
                        "   $addFields: { " +
                        "       presentationRef: { $concat: [ '$eventId', ':', '$externalId' ] } " +
                        "   }" +
                        "}")
                .and("{ " +
                        "   $lookup: { " +
                        "       from: '" + subscriptionCollection.getName() + "', " +
                        "       localField: 'presentationRef'," +
                        "       foreignField: 'presentationRef'," +
                        "       as: 'subscriptions'" +
                        "   }" +
                        "}"
                )
                .and("{" +
                        "   $match: { " +
                        "       'subscriptions.userId': #" +
                        "   }" +
                        "}", user.getId())
                .and("{" +
                        "   $group: { _id: '$eventId', presentations: { $push: '$$ROOT' } }" +
                        "}")
                .as(EventPresentations.class);
    }

    /**
     * CRUD
     */

    protected SubscriptionDbWrite addOrUpdateSubscription(String presentationId) {

        User user = AuthModule.currentUser().get();

        Presentation presentation = Reference.<Presentation>of(Type.presentation, presentationId).get();
        String presentationRef = PresentationRef.buildPresentationBusinessRef(presentation);

        Subscription subscription = new Subscription()
                .setPresentationRef(presentationRef)
                .setUserId(user.getId());

        subscriptionCollection.get()
                .update("{ presentationRef: #, userId: # }", presentationRef, user.getId())
                .upsert()
                .with(subscription);

        return new SubscriptionDbWrite(presentation, subscription);
    }


    protected Subscription deleteSubscription(String presentationId) {

        User user = AuthModule.currentUser().get();
        String presentationRef = PresentationRef.getPresentationRef(presentationId);

        Subscription existingSubscription = subscriptionCollection
                .get()
                .findOne("{ presentationRef: #, userId: # }", presentationRef, user.getId()).as(Subscription.class);

        if (existingSubscription == null) {
            throw new WebException(HttpStatus.NOT_FOUND);
        }

        subscriptionCollection
                .get()
                .remove("{ presentationRef: #, userId: # }", presentationRef, user.getId());

        return existingSubscription;
    }

    public static class SubscriptionDbWrite {

        public Presentation presentation;

        public Subscription subscription;

        public SubscriptionDbWrite(Presentation presentation, Subscription subscription) {
            this.presentation = presentation;
            this.subscription = subscription;
        }
    }
}
