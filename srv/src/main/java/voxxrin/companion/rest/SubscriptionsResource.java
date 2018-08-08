package voxxrin.companion.rest;

import com.google.common.base.Optional;
import restx.annotations.*;
import restx.factory.Component;
import voxxrin.companion.auth.AuthModule;
import voxxrin.companion.domain.EventPresentations;
import voxxrin.companion.domain.Subscription;
import voxxrin.companion.domain.User;
import voxxrin.companion.persistence.FavoritesService;
import voxxrin.companion.persistence.RemindersService;

@Component
@RestxResource
public class SubscriptionsResource {

    private final RemindersService remindersService;
    private final FavoritesService favoritesService;

    public SubscriptionsResource(RemindersService remindersService, FavoritesService favoritesService) {
        this.remindersService = remindersService;
        this.favoritesService = favoritesService;
    }

    @POST("/remindme")
    public Subscription requestRemindMe(@Param(kind = Param.Kind.QUERY) String presentationId) {
        return remindersService.addOrUpdateRemindMe(presentationId).subscription;
    }

    @DELETE("/remindme")
    public Subscription deleteRemindMe(@Param(kind = Param.Kind.QUERY) String presentationId) {
        return remindersService.deleteRemindMe(presentationId);
    }

    @GET("/favorite")
    public Iterable<EventPresentations> getFavoritePresentations() {
        Optional<User> user = AuthModule.currentUser();
        if (!user.isPresent()) {
            throw new IllegalStateException("no user found");
        }
        return favoritesService.findFavoritedPresentations(user.get());
    }

    @POST("/favorite")
    public Subscription requestFavorite(@Param(kind = Param.Kind.QUERY) String presentationId) {
        return favoritesService.addOrUpdateFavorite(presentationId).subscription;
    }

    @DELETE("/favorite")
    public Subscription deleteFavorite(@Param(kind = Param.Kind.QUERY) String presentationId) {
        return favoritesService.deleteFavorite(presentationId);
    }
}
