package voxxrin.companion.persistence;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import restx.factory.Component;
import restx.jongo.JongoCollection;
import voxxrin.companion.auth.AuthModule;
import voxxrin.companion.domain.AttachedContent;
import voxxrin.companion.domain.Presentation;
import voxxrin.companion.domain.Type;
import voxxrin.companion.domain.User;
import voxxrin.companion.domain.technical.ElementURI;
import voxxrin.companion.utils.PresentationRef;

import javax.inject.Named;
import java.util.regex.Matcher;

@Component
public class PresentationsDataService extends DataService<Presentation> {

    private final JongoCollection collection;
    private final RemindersService remindersService;
    private final FavoritesService favoritesService;

    private final Function<Presentation, Presentation> USER_PRESENTATION_FUNCTOR = new Function<Presentation, Presentation>() {
        @Override
        public Presentation apply(Presentation input) {
            Optional<User> user = AuthModule.currentUser();
            if (user.isPresent()) {
                input.setReminded(remindersService.isReminded(user.get(), input));
                input.setFavorite(favoritesService.isFavorite(user.get(), input));
            }
            input.setRemindMeCount(remindersService.getRemindersCount(input));
            input.setFavoriteCount(favoritesService.getFavoritesCount(input));
            return input;
        }
    };

    public PresentationsDataService(@Named(Presentation.COLLECTION) JongoCollection collection,
                                    RemindersService remindersService,
                                    FavoritesService favoritesService) {
        super(collection, Presentation.class);
        this.collection = collection;
        this.remindersService = remindersService;
        this.favoritesService = favoritesService;
    }

    @Override
    public Iterable<Presentation> findAllAndSort(String query, String sorting, Object... params) {
        return Iterables.transform(super.findAllAndSort(query, sorting, params), USER_PRESENTATION_FUNCTOR);
    }

    @Override
    public Presentation find(String query, Object... params) {
        Presentation input = super.find(query, params);
        if (input != null) {
            return USER_PRESENTATION_FUNCTOR.apply(input);
        }
        return null;
    }

    @Override
    public Optional<Presentation> findById(String id) {
        return super.findById(id).transform(USER_PRESENTATION_FUNCTOR);
    }

    public Presentation attachReleasedContent(Presentation presentation, AttachedContent content) {
        return collection.get()
                .findAndModify("{ _id: # }", new ObjectId(presentation.getKey()))
                .with("{ $push: { releasedContents: # }}", content)
                .returnNew()
                .as(Presentation.class);
    }

    public Presentation findByRef(String presentationRef) {

        Matcher matcher = PresentationRef.PATTERN.matcher(presentationRef);
        if (!matcher.matches()) {
            return null;
        }

        String eventId = matcher.group(1);
        String externalId = matcher.group(2);

        return find("{ eventId: #, externalId: # }", eventId, externalId);
    }

    public Iterable<Presentation> findByEvent(String eventId) {
        return findAllAndSort("{ event: # }", "{ from: 1 }", ElementURI.of(Type.event, eventId).toString());
    }

    public Iterable<Presentation> findByEventAlias(String eventAlias) {
        return findAllAndSort("{ eventId: # }", "{ from: 1 }", eventAlias);
    }

    public Iterable<Presentation> findByDay(String dayId) {
        return findAllAndSort("{ day: # }", "{ from: 1 }", ElementURI.of(Type.day, dayId).toString());
    }

    public Iterable<Presentation> search(String eventId, String title) {
        return findAll("{ eventId: #, title: { $regex: #, $options: 'i' } }", eventId, title);
    }

    public Iterable<Presentation> findNext(int minutes) {
        DateTime now = DateTime.now();
        return findAll("{ from: { $gte: #, $lte: # } }", now.toDate(), now.plusMinutes(minutes).toDate());
    }
}
