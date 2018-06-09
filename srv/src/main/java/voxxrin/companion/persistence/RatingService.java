package voxxrin.companion.persistence;

import com.google.common.base.Optional;
import org.joda.time.DateTime;
import restx.WebException;
import restx.factory.Component;
import restx.http.HttpStatus;
import restx.jongo.JongoCollection;
import voxxrin.companion.auth.AuthModule;
import voxxrin.companion.domain.*;
import voxxrin.companion.domain.technical.Reference;
import voxxrin.companion.utils.PresentationRef;

import javax.inject.Named;
import java.util.Set;

@Component
public class RatingService {

    private final JongoCollection items;
    private final JongoCollection ratings;

    public RatingService(@Named(RatingItem.COLLECTION) JongoCollection items, @Named(Rating.COLLECTION) JongoCollection ratings) {
        this.items = items;
        this.ratings = ratings;
    }

    public Iterable<RatingItem> findAllAvailableItems() {
        return items.get().find().as(RatingItem.class);
    }

    public Iterable<Rating> findPresentationRatings(String presentationId) {
        Presentation presentation = Reference.<Presentation>of(Type.presentation, presentationId).get();
        return ratings.get()
                .find("{ presentationRef: # }", PresentationRef.buildPresentationBusinessRef(presentation))
                .as(Rating.class);
    }

    public Iterable<Rating> findPresentationRatings(String presentationId, User user) {
        Presentation presentation = Reference.<Presentation>of(Type.presentation, presentationId).get();
        String presentationRef = PresentationRef.buildPresentationBusinessRef(presentation);
        return ratings.get()
                .find("{ presentationRef: #, userId: # }", presentationRef, user.getId())
                .as(Rating.class);
    }

    public Iterable<Rating> findEventRatings(String eventId, String itemKey) {
        return ratings.get()
                .find("{ presentationRef: { $regex: # }, 'ratingItems.key': # }", String.format("%s:.*", eventId), itemKey)
                .as(Rating.class);
    }

    public Rating ratePresentation(String presentationId, Set<RatingItem> ratingItems) {

        if (!AuthModule.currentUser().isPresent()) {
            throw new WebException(HttpStatus.UNAUTHORIZED);
        }

        Optional<Presentation> presentation = Reference.<Presentation>of(Type.presentation, presentationId).maybeGet();
        if (!presentation.isPresent()) {
            throw new WebException(HttpStatus.NOT_FOUND);
        }

        String userId = AuthModule.currentUser().get().getId();

        String presentationRef = PresentationRef.buildPresentationBusinessRef(presentation.get());
        Rating rating = new Rating()
                .setDateTime(DateTime.now())
                .setPresentationRef(presentationRef)
                .setRatingItems(ratingItems)
                .setUserId(userId);

        ratings.get()
                .update("{ presentationRef: #, userId: # }", presentationRef, userId)
                .upsert()
                .with(rating);

        return rating;
    }
}
