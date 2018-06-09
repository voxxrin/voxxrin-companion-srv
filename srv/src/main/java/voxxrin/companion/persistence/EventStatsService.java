package voxxrin.companion.persistence;

import com.google.common.base.Function;
import com.google.common.collect.*;
import restx.factory.Component;
import voxxrin.companion.domain.*;
import voxxrin.companion.domain.technical.Reference;
import voxxrin.companion.rest.PresentationsResource;

import java.util.*;

@Component
public class EventStatsService {

    private final PresentationsDataService presentationsDataService;
    private final PresentationsResource presentationsResource;
    private final RemindersService remindersService;
    private final FavoritesService favoritesService;
    private final RatingService ratingService;

    public EventStatsService(PresentationsDataService presentationsDataService,
                             PresentationsResource presentationsResource,
                             RemindersService remindersService,
                             FavoritesService favoritesService,
                             RatingService ratingService) {
        this.presentationsDataService = presentationsDataService;
        this.presentationsResource = presentationsResource;
        this.remindersService = remindersService;
        this.favoritesService = favoritesService;
        this.ratingService = ratingService;
    }

    public EventStats buildPublic(Event event) {

        EventStats eventStats = new EventStats();

        ArrayList<Presentation> presentations = Lists.newArrayList(presentationsResource.getEventPresentations(event.getKey()));

        return eventStats
                .setEventId(event.getEventId())
                .setEventName(event.getName())
                .setTalksCount(presentations.size())
                .setSpeakersCount(speakersCount(presentations));
    }

    public EventStats build(Event event) {

        EventStats eventStats = new EventStats();
        List<Subscription> reminders = Lists.newArrayList(remindersService.getReminders(event));
        List<Subscription> favorites = Lists.newArrayList(favoritesService.getFavorites(event));

        ArrayList<Presentation> presentations = Lists.newArrayList(presentationsResource.getEventPresentations(event.getKey()));

        return eventStats
                .setEventId(event.getEventId())
                .setEventName(event.getName())
                .setTalksCount(presentations.size())
                .setSpeakersCount(speakersCount(presentations))
                .setFavoritesCount(favorites.size())
                .setUsersWithFavoritesCount(usersWithSubscriptionCount(favorites))
                .setUsersWithRemindersCount(usersWithSubscriptionCount(reminders))
                .setRemindersCount(reminders.size())
                .setTopRatings(computeTopRating(event))
                .setTopFavoritedPresentation(topOccurence(favorites))
                .setTopRemindedPresentation(topOccurence(reminders));
    }

    private int usersWithSubscriptionCount(List<Subscription> subs) {
        return FluentIterable.from(subs).transform(new Function<Subscription, String>() {
            @Override
            public String apply(Subscription input) {
                return input.getUserId();
            }
        }).toSet().size();
    }

    private int speakersCount(ArrayList<Presentation> presentations) {
        Set<String> speakers = new HashSet<>();
        for (Presentation presentation : presentations) {
            for (Reference<Speaker> speakerReference : presentation.getSpeakers()) {
                speakers.add(speakerReference.getUri().getKey());
            }
        }
        return speakers.size();
    }

    private <T extends HasPresentationRef> Presentation topOccurence(List<T> elts) {

        ImmutableListMultimap<String, T> indexedElts = Multimaps.index(elts, new Function<HasPresentationRef, String>() {
            @Override
            public String apply(HasPresentationRef input) {
                return input.getPresentationRef();
            }
        });

        int max = 0;
        String presentationRef = null;
        for (Map.Entry<String, Collection<T>> entry : indexedElts.asMap().entrySet()) {
            int size = entry.getValue().size();
            if (size > max) {
                max = size;
                presentationRef = entry.getKey();
            }
        }

        if (presentationRef != null) {
            return presentationsDataService.findByRef(presentationRef);
        }

        return null;
    }

    private Map<String, Presentation> computeTopRating(Event event) {

        List<RatingItem> ratingItems = Lists.newArrayList(ratingService.findAllAvailableItems());
        Map<String, Presentation> presentations = new HashMap<>();

        for (final RatingItem ratingItem : ratingItems) {
            ImmutableSet<String> presentationRefs = FluentIterable
                    .from(ratingService.findEventRatings(event.getEventId(), ratingItem.getKey()))
                    .transform(new Function<Rating, String>() {
                        @Override
                        public String apply(Rating input) {
                            return input.getPresentationRef();
                        }
                    })
                    .toSet();
            LinkedHashMultiset<String> objects = LinkedHashMultiset.create(presentationRefs);
            String maxOccuredPresentationRef = Iterables.getFirst(Multisets.copyHighestCountFirst(objects), null);
            Presentation presentation = null;
            if (maxOccuredPresentationRef != null) {
                presentation = presentationsDataService.findByRef(maxOccuredPresentationRef);
            }
            presentations.put(ratingItem.getKey(), presentation);
        }

        return presentations;
    }
}
