package voxxrin.companion.rest;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import restx.annotations.*;
import restx.factory.Component;
import voxxrin.companion.auth.AuthModule;
import voxxrin.companion.domain.EventPresentations;
import voxxrin.companion.domain.Subscription;
import voxxrin.companion.domain.User;
import voxxrin.companion.persistence.FavoritesService;
import voxxrin.companion.persistence.RemindersService;
import voxxrin.companion.services.PushService;

@Component
@RestxResource
public class SubscriptionsResource {

    private final RemindersService remindersService;
    private final FavoritesService favoritesService;
    private final PushService pushService;

    public SubscriptionsResource(RemindersService remindersService, FavoritesService favoritesService, PushService pushService) {
        this.remindersService = remindersService;
        this.favoritesService = favoritesService;
        this.pushService = pushService;
    }

    @POST("/remindme")
    public Subscription requestRemindMe(@Param(kind = Param.Kind.QUERY) String presentationId,
                                        @Param(kind = Param.Kind.QUERY) Optional<String> deviceToken) {
        Subscription subscription = remindersService.addOrUpdateRemindMe(presentationId).subscription;
        if (deviceToken.isPresent() && !Strings.isNullOrEmpty(deviceToken.get())) {
            pushService.subscribeToTalkContent(subscription, deviceToken.get());
        }
        return subscription;
    }

    @DELETE("/remindme")
    public Subscription deleteRemindMe(@Param(kind = Param.Kind.QUERY) String presentationId,
                                       @Param(kind = Param.Kind.QUERY) Optional<String> deviceToken) {
        Subscription subscription = remindersService.deleteRemindMe(presentationId);
        if (deviceToken.isPresent() && !Strings.isNullOrEmpty(deviceToken.get())) {
            pushService.unsubscribeFromTalkContent(subscription, deviceToken.get());
        }
        return subscription;
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
    public Subscription requestFavorite(@Param(kind = Param.Kind.QUERY) String presentationId,
                                        @Param(kind = Param.Kind.QUERY) Optional<String> deviceToken) {
        Subscription subscription = favoritesService.addOrUpdateFavorite(presentationId).subscription;
        if (deviceToken.isPresent() && !Strings.isNullOrEmpty(deviceToken.get())) {
            pushService.subscribeToTalkBeginning(subscription, deviceToken.get());
        }
        return subscription;
    }

    @DELETE("/favorite")
    public Subscription deleteFavorite(@Param(kind = Param.Kind.QUERY) String presentationId,
                                       @Param(kind = Param.Kind.QUERY) Optional<String> deviceToken) {
        Subscription subscription = favoritesService.deleteFavorite(presentationId);
        if (deviceToken.isPresent()) {
            pushService.unsubscribeFromTalkBeginning(subscription, deviceToken.get());
        }
        return subscription;
    }
}
