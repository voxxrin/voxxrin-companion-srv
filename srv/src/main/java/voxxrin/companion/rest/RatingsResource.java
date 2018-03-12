package voxxrin.companion.rest;

import restx.annotations.GET;
import restx.annotations.PUT;
import restx.annotations.Param;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;
import voxxrin.companion.domain.Rating;
import voxxrin.companion.persistence.RatingService;
import voxxrin.companion.domain.Rating;
import voxxrin.companion.persistence.RatingService;

@Component
@RestxResource
public class RatingsResource {

    private final RatingService ratingService;

    public RatingsResource(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @GET("/ratings/{presentationId}")
    @PermitAll
    public Iterable<Rating> getRatings(@Param(kind = Param.Kind.PATH) String presentationId) {
        return ratingService.findPresentationRatings(presentationId);
    }

    @PUT("/ratings/{presentationId}")
    public Rating ratePresentation(@Param(kind = Param.Kind.PATH) String presentationId,
                                   @Param(kind = Param.Kind.QUERY) int rate) {
        return ratingService.ratePresentation(presentationId, rate);
    }

}
