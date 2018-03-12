package voxxrin.companion.rest;

import restx.annotations.GET;
import restx.annotations.Param;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;
import voxxrin.companion.domain.Presentation;
import voxxrin.companion.domain.Room;
import voxxrin.companion.domain.Speaker;
import voxxrin.companion.persistence.PresentationsDataService;
import voxxrin.companion.persistence.RoomsDataService;
import voxxrin.companion.persistence.SpeakersDataService;
import voxxrin.companion.domain.Presentation;
import voxxrin.companion.domain.Room;
import voxxrin.companion.domain.Speaker;
import voxxrin.companion.persistence.PresentationsDataService;
import voxxrin.companion.persistence.RoomsDataService;
import voxxrin.companion.persistence.SpeakersDataService;

@Component
@RestxResource(value = "/search")
public class SearchResource {

    private final RoomsDataService roomsDataService;
    private final PresentationsDataService presentationsDataService;
    private final SpeakersDataService speakersDataService;

    public SearchResource(RoomsDataService roomsDataService,
                          PresentationsDataService presentationsDataService,
                          SpeakersDataService speakersDataService) {
        this.roomsDataService = roomsDataService;
        this.presentationsDataService = presentationsDataService;
        this.speakersDataService = speakersDataService;
    }

    @GET("/events/:eventId/rooms")
    @PermitAll
    public Iterable<Room> findRooms(@Param(kind = Param.Kind.PATH) String eventId,
                                    @Param(kind = Param.Kind.QUERY) String fullName) {
        return roomsDataService.search(eventId, fullName);
    }

    @GET("/events/:eventId/presentations")
    @PermitAll
    public Iterable<Presentation> findPresentations(@Param(kind = Param.Kind.PATH) String eventId,
                                                    @Param(kind = Param.Kind.QUERY) String title) {
        return presentationsDataService.search(eventId, title);
    }

    @GET("/events/:eventId/speakers")
    @PermitAll
    public Iterable<Speaker> findSpeakers(@Param(kind = Param.Kind.PATH) String eventId,
                                          @Param(kind = Param.Kind.QUERY) String name) {
        return speakersDataService.search(eventId, name);
    }
}
