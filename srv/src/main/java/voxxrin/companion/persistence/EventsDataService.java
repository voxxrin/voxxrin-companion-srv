package voxxrin.companion.persistence;

import com.google.common.base.Optional;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import restx.factory.Component;
import restx.jongo.JongoCollection;
import voxxrin.companion.domain.Event;
import voxxrin.companion.domain.EventTemporality;

import javax.inject.Named;

@Component
public class EventsDataService extends DataService<Event> {

    public EventsDataService(@Named(Event.COLLECTION) JongoCollection collection) {
        super(collection, Event.class);
    }

    public Iterable<Event> findByTemporality(Optional<String> temporality) {
        if (temporality.isPresent()) {
            if (EventTemporality.FUTURE.name().equalsIgnoreCase(temporality.get())) {
                return findAllAndSort("{ to: { $gte: # } }", "{ from: 1 }", DateTime.now().toDate());
            } else if (EventTemporality.PAST.name().equalsIgnoreCase(temporality.get())) {
                return findAllAndSort("{ to: { $lte: # } }", "{ from: 1 }", DateTime.now().toDate());
            }
        }
        return findAllAndSort("{ from: 1 }");
    }

    public Optional<Event> findByAlias(String id) {
        return Optional.fromNullable(find("{ eventId: # }", id));
    }

    public Optional<Event> updateEventData(ObjectId id, Event event) {
        return Optional.fromNullable(getCollection().get()
                .findAndModify("{ _id: # }", id)
                .with("{ $set: { links: # } }", event.getLinks())
                .as(Event.class)
        );
    }
}
