package voxxrin.companion.rest;

import com.google.common.base.Optional;
import org.bson.types.ObjectId;
import restx.RestxRequest;
import restx.annotations.*;
import restx.factory.Component;
import restx.security.PermitAll;
import restx.security.RestxSecurityManager;
import restx.security.RolesAllowed;
import voxxrin.companion.domain.Event;
import voxxrin.companion.persistence.EventsDataService;
import voxxrin.companion.security.Permissions;

@Component
@RestxResource
public class EventsResource {

    private final RestxSecurityManager securityManager;
    private final EventsDataService eventsDataService;

    public EventsResource(RestxSecurityManager securityManager, EventsDataService dataService) {
        this.securityManager = securityManager;
        this.eventsDataService = dataService;
    }

    @GET("/events")
    @PermitAll
    public Iterable<Event> getAllEvents(Optional<String> mode) {
        return eventsDataService.findByTemporality(mode);
    }

    @GET("/events/{id}")
    @PermitAll
    public Optional<Event> getEvent(String id) {
        if (ObjectId.isValid(id)) {
            return eventsDataService.findById(id);
        } else {
            return eventsDataService.findByAlias(id);
        }
    }

    @POST("/events")
    @RolesAllowed({"ADM", "restx-admin"})
    public Event saveEvent(Event event) {
        return eventsDataService.save(event);
    }

    @PUT("/events/:id")
    public Optional<Event> updateEvent(@Param(kind = Param.Kind.PATH) String id,
                                       @Param(kind = Param.Kind.BODY) Event event,
                                       @Param(kind = Param.Kind.CONTEXT) RestxRequest request) {
        Optional<Event> dbEvent = getEvent(id);
        if (!dbEvent.isPresent()) {
            throw new IllegalArgumentException(String.format("event %s not found", id));
        }
        securityManager.check(request, Permissions.buildEventAdminPermission(dbEvent.get()));
        return eventsDataService.updateEventData(new ObjectId(id), event);
    }
}
