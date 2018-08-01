package voxxrin.companion.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EventPresentations {

    private String eventId;

    private Iterable<Presentation> presentations;

    public String getEventId() {
        return eventId;
    }

    @JsonCreator
    public EventPresentations(@JsonProperty("_id") String id, @JsonProperty("presentations") Iterable<Presentation> presentations) {
        this.eventId = id;
        this.presentations = presentations;
    }

    public EventPresentations setEventId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    public Iterable<Presentation> getPresentations() {
        return presentations;
    }

    public EventPresentations setPresentations(Iterable<Presentation> presentations) {
        this.presentations = presentations;
        return this;
    }
}
