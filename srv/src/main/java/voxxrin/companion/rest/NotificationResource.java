package voxxrin.companion.rest;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import restx.annotations.PUT;
import restx.annotations.Param;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.RolesAllowed;
import voxxrin.companion.domain.Presentation;
import voxxrin.companion.persistence.PresentationsDataService;
import voxxrin.companion.services.PushService;

import static org.slf4j.LoggerFactory.getLogger;

@Component
@RestxResource("/notification")
public class NotificationResource {

    private static final Logger logger = getLogger(NotificationResource.class);

    private final PresentationsDataService presentationsDataService;
    private final PushService pushService;

    public NotificationResource(PresentationsDataService presentationsDataService, PushService pushService) {
        this.presentationsDataService = presentationsDataService;
        this.pushService = pushService;
    }

    @PUT("/trigger")
    @RolesAllowed("restx-admin")
    public void trigger(@Param(kind = Param.Kind.QUERY) Optional<String> allFuture) {
        ImmutableSet<Presentation> presentations = FluentIterable
                .from(findNextPresentations(allFuture))
                .toSet();
        for (Presentation presentation : presentations) {
            logger.info("talk {} - publishing beginning", presentation.getExternalId());
            pushService.publishTalkBeginning(presentation);
        }
    }

    private Iterable<Presentation> findNextPresentations(Optional<String> allFuture) {
        if (allFuture.isPresent() && Boolean.valueOf(allFuture.get())) {
            return presentationsDataService.findNext(15000000);
        } else {
            return presentationsDataService.findNext(15);
        }
    }
}
