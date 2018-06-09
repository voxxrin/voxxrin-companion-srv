package voxxrin.companion.rest;

import restx.annotations.GET;
import restx.annotations.PUT;
import restx.annotations.Param;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;
import voxxrin.companion.auth.AuthModule;
import voxxrin.companion.domain.Rating;
import voxxrin.companion.domain.RatingItem;
import voxxrin.companion.domain.User;
import voxxrin.companion.persistence.RatingService;

import java.util.Set;

@Component
@RestxResource("/rating")
public class RatingResource {

    private final RatingService ratingService;

    public RatingResource(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @GET("/item")
    @PermitAll
    public Iterable<RatingItem> findAllItems() {
        return ratingService.findAllAvailableItems();
    }

    @GET("/presentation/:id")
    public Iterable<Rating> findPresentationRating(String id) {
        User currentUser = AuthModule.currentUser().get();
        return ratingService.findPresentationRatings(id, currentUser);
    }

    @PUT("/presentation/:id")
    public Rating ratePresentation(@Param(kind = Param.Kind.PATH) String id,
                                   @Param(kind = Param.Kind.BODY) Set<RatingItem> items) {
        return ratingService.ratePresentation(id, items);
    }
}
