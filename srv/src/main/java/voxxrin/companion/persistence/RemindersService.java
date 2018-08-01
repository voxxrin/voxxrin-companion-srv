package voxxrin.companion.persistence;

import com.google.common.collect.Iterables;
import restx.factory.Component;
import restx.jongo.JongoCollection;
import voxxrin.companion.domain.Event;
import voxxrin.companion.domain.Presentation;
import voxxrin.companion.domain.Subscription;
import voxxrin.companion.domain.User;

import javax.inject.Named;

@Component
public class RemindersService extends SubscriptionService {

    public RemindersService(@Named(Subscription.REMINDERS_COLLECTION) JongoCollection reminders,
                            @Named(Presentation.COLLECTION) JongoCollection presentations) {
        super(reminders, presentations);
    }

    /**
     * Read
     */

    public boolean isReminded(User user, Presentation presentation) {
        return isSubscribed(user, presentation);
    }

    public long getRemindersCount(Presentation presentation) {
        return Iterables.size(getSubscriptions(presentation));
    }

    public Iterable<Subscription> getReminders(Presentation presentation) {
        return getSubscriptions(presentation);
    }

    public Iterable<Subscription> getReminders(Event event) {
        return getSubscriptions(event);
    }

    /**
     * CRUD
     */

    public SubscriptionDbWrite addOrUpdateRemindMe(String presentationId) {
        return addOrUpdateSubscription(presentationId);
    }

    public Subscription deleteRemindMe(String presentationId) {
        return deleteSubscription(presentationId);
    }
}
