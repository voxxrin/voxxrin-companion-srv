package voxxrin.companion.persistence;

import com.google.common.collect.Iterables;
import restx.factory.Component;
import restx.jongo.JongoCollection;
import voxxrin.companion.domain.*;

import javax.inject.Named;

@Component
public class FavoritesService extends SubscriptionService {

    public FavoritesService(@Named(Subscription.FAVORITES_COLLECTION) JongoCollection favorites,
                            @Named(Presentation.COLLECTION) JongoCollection presentations) {
        super(favorites, presentations);
    }

    /**
     * Read
     */

    public boolean isFavorite(User user, Presentation presentation) {
        return isSubscribed(user, presentation);
    }

    public long getFavoritesCount(Presentation presentation) {
        return Iterables.size(getFavorites(presentation));
    }

    public Iterable<Subscription> getFavorites(Presentation presentation) {
        return getSubscriptions(presentation);
    }

    public Iterable<Subscription> getFavorites(Event event) {
        return getSubscriptions(event);
    }

    public Iterable<EventPresentations> findFavoritedPresentations(User user) {
        return findSubscribedPresentations(user);
    }

    /**
     * CRUD
     */

    public SubscriptionDbWrite addOrUpdateFavorite(String presentationId) {
        return addOrUpdateSubscription(presentationId);
    }

    public Subscription deleteFavorite(String presentationId) {
        return deleteSubscription(presentationId);
    }
}
