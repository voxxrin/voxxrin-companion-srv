package voxxrin.companion.persistence;

import restx.factory.Component;
import restx.jongo.JongoCollection;
import voxxrin.companion.domain.Room;
import voxxrin.companion.domain.Room;

import javax.inject.Named;

@Component
public class RoomsDataService extends DataService<Room> {

    public RoomsDataService(@Named(Room.COLLECTION) JongoCollection collection) {
        super(collection, Room.class);
    }

    public Iterable<Room> search(String eventId, String fullName) {
        return findAll("{ eventId: #, fullName: { $regex : #, $options: 'i' } }", eventId, fullName);
    }
}
