package crawlers.impl;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import crawlers.AbstractHttpCrawler;
import crawlers.CrawlingResult;
import crawlers.configuration.CrawlingConfiguration;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import restx.factory.Component;
import voxxrin.companion.domain.*;
import voxxrin.companion.domain.technical.Reference;

import java.io.IOException;
import java.util.*;

@Component
public class BDXIOScheduleCrawler extends AbstractHttpCrawler {

    private static final String BASE_URL = "https://raw.githubusercontent.com/bdxio/bdxio.github.io/2019/src/static/static/schedule.json";

    public BDXIOScheduleCrawler() {
        super("bdxio-schedule", ImmutableList.of("bdxio-publisher"));
    }

    @Override
    public CrawlingResult crawl(CrawlingConfiguration configuration) throws IOException {

        HttpRequest httpRequest = HttpRequest.get(BASE_URL);
        Schedule schedule = MAPPER.readValue(httpRequest.body(), Schedule.class);

        DateTime from = getFrom(schedule);
        DateTime to = getFrom(schedule).plusDays(1).minusHours(1);

        Event event = createEvent(configuration, from, to);
        Day day = createDay(event);
        Map<Integer, Speaker> speakers = createSpeakers(schedule);
        Map<String, Room> rooms = createRooms(schedule);

        List<Presentation> presentations = createPresentations(schedule, day, speakers, rooms);

        CrawlingResult crawlingResult = new CrawlingResult(event);
        crawlingResult.getDays().add(day);
        crawlingResult.getRooms().addAll(rooms.values());
        crawlingResult.getSpeakers().addAll(speakers.values());
        crawlingResult.getPresentations().addAll(presentations);

        return crawlingResult;
    }

    private List<Presentation> createPresentations(Schedule schedule, Day day, Map<Integer, Speaker> speakers, Map<String, Room> rooms) {
        Reference<Day> dayRef = Reference.of(Type.day, day.getKey());
        List<Presentation> presentations = new ArrayList<>();
        for (ScheduleSession session : schedule.sessions.values()) {
            List<Reference<Speaker>> speakerRefs = getPresentationSpeakers(speakers, session);
            Reference<Room> roomRef = Reference.of(Type.room, rooms.get(session.trackTitle).getKey());
            presentations.add(
                    (Presentation) new Presentation()
                            .setTitle(session.title)
                            .setSummary(session.description)
                            .setEvent(day.getEvent())
                            .setLocation(roomRef)
                            .setDay(dayRef)
                            // uggly  ...
                            .setFrom(session.startTime.minusHours(1))
                            .setTo(session.endTime.minusHours(1))
                            .setSpeakers(speakerRefs)
                            .setExternalId(String.valueOf(session.id))
                            .setKey(new ObjectId().toString())

            );
        }
        return presentations;
    }

    private List<Reference<Speaker>> getPresentationSpeakers(Map<Integer, Speaker> speakers, ScheduleSession session) {
        List<Speaker> speakersList = new ArrayList<>();
        for (Integer speakerId : session.speakers) {
            Speaker speaker = speakers.get(speakerId);
            speakersList.add(speaker);
        }
        Collections.sort(speakersList, new Comparator<Speaker>() {
            @Override
            public int compare(Speaker o1, Speaker o2) {
                String[] o1Names = o1.getName().split("\\s");
                String[] o2Names = o2.getName().split("\\s");
                return o1Names[o1Names.length - 1].compareTo(o2Names[o2Names.length - 1]);
            }
        });
        List<Reference<Speaker>> speakerRefs = new ArrayList<>();
        for (Speaker speaker : speakersList) {
            Reference<Speaker> speakerRef = Reference.of(Type.speaker, speaker.getKey());
            speakerRefs.add(speakerRef);
        }
        return speakerRefs;
    }

    private Event createEvent(CrawlingConfiguration configuration, DateTime from, DateTime to) {
        return (Event) new Event()
                .setEventId(configuration.getEventId())
                .setName(configuration.getEventName())
                .setLocation(configuration.getLocation())
                .setImageUrl("https://www.bdx.io/img/png/logo-color%20(72%20ppp).png")
                .setFrom(from)
                .setTo(to)
                .setKey(new ObjectId().toString());
    }

    private DateTime getFrom(Schedule schedule) {
        ImmutableList<DateTime> dateTimes = FluentIterable.from(schedule.sessions.values())
                .transform(new Function<ScheduleSession, DateTime>() {
                    @Override
                    public DateTime apply(ScheduleSession input) {
                        return input.startTime;
                    }
                })
                .toList();
        return Ordering.natural().min(dateTimes).withTimeAtStartOfDay();
    }

    private Day createDay(Event event) {
        Reference<Event> eventRef = Reference.of(Type.event, event.getKey());
        Day day = (Day) new Day().setEvent(eventRef).setKey(new ObjectId().toString());
        DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/yyyy");
        DateTime date = event.getFrom();
        day.setName(fmt.print(date));
        day.setDate(date);
        return day;
    }

    private Map<String, Room> createRooms(Schedule schedule) {
        Set<String> roomNames = new HashSet<>();
        for (ScheduleSession input : schedule.sessions.values()) {
            roomNames.add(input.trackTitle);
        }
        Map<String, Room> rooms = new HashMap<>();
        for (String roomName : roomNames) {
            Room room = (Room) new Room()
                    .setName(roomName)
                    .setFullName(roomName)
                    .setKey(new ObjectId().toString());
            rooms.put(roomName, room);
        }
        return rooms;
    }

    private Map<Integer, Speaker> createSpeakers(Schedule schedule) {
        Map<Integer, Speaker> speakers = new HashMap<>();
        for (ScheduleSpeaker input : schedule.speakers.values()) {
            Speaker speaker = (Speaker) new Speaker()
                    .setName(input.name)
                    .setBio(input.bio)
                    .setCompany(input.company)
                    .setAvatarUrl(input.photoUrl)
                    .setKey(new ObjectId().toString());
            speakers.put(input.id, speaker);
        }
        return speakers;
    }

    private static class Schedule {
        public Map<Integer, ScheduleSession> sessions;
        public Map<Integer, ScheduleSpeaker> speakers;
    }

    private static class ScheduleSession {
        public Integer id;
        public String description;
        public Set<String> tags;
        public String title;
        public DateTime startTime;
        public DateTime endTime;
        public String trackTitle;
        public List<Integer> speakers;
    }

    private static class ScheduleSpeaker {
        public Integer id;
        public String name;
        public String company;
        public String bio;
        public String address;
        public String photoUrl;
    }
}

