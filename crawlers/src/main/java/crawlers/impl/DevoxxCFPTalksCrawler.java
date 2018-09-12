package crawlers.impl;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import crawlers.CrawlingResult;
import crawlers.configuration.CrawlingConfiguration;
import crawlers.utils.Functions;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import voxxrin.companion.domain.*;
import voxxrin.companion.domain.technical.Reference;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DevoxxCFPTalksCrawler extends DevoxxCFPCrawler {

    private final String baseUrl;

    public DevoxxCFPTalksCrawler(String id, List<String> roles, String baseUrl) {
        super(id, roles, baseUrl);
        this.baseUrl = baseUrl;
    }

    @Override
    public CrawlingResult crawl(CrawlingConfiguration configuration) throws IOException {

        String eventUrl = baseUrl + configuration.getExternalEventRef();
        String talksUrl = eventUrl + "/talks/";
        String daysUrl = eventUrl + "/schedules/";

        CFPEvent cfpEvent = MAPPER.readValue(httpGet(eventUrl, configuration).body(), DevoxxCFPCrawler.CFPEvent.class);
        List<CFPTalk> cfpTalks = MAPPER.readValue(httpGet(talksUrl, configuration).body(), buildCollectionType(CFPTalk.class));
        List<CFPLinks> cfpSpeakerLinks = getCfpSpeakerLinks(cfpTalks);
        CFPLinks cfpDayLinks = MAPPER.readValue(httpGet(daysUrl, configuration).body(), CFPLinks.class);

        CrawlingResult crawlingResult = new CrawlingResult(cfpEvent.toStdEvent());
        crawlingResult.getRooms().add((Room) new Room().setName("N/C").setFullName("N/C").setKey(new ObjectId().toString()));

        crawlSchedules(cfpDayLinks, crawlingResult, configuration);
        Map<String, Speaker> speakers = getSpeakersMap(cfpSpeakerLinks, configuration);
        crawlingResult.getSpeakers().addAll(speakers.values());
        crawlPresentations(cfpTalks, speakers, crawlingResult);

        setEventTemporalLimits(crawlingResult);

        return crawlingResult;
    }

    private void crawlPresentations(List<CFPTalk> cfpTalks, final Map<String, Speaker> speakers, CrawlingResult crawlingResult) {

        Reference<Event> eventRef = Reference.of(Type.event, crawlingResult.getEvent().getKey());
        Reference<Room> roomRef = Reference.of(Type.room, crawlingResult.getRooms().get(0).getKey());
        Day day = crawlingResult.getDays().get(0);
        Reference<Day> dayRef = Reference.of(Type.day, day.getKey());

        int partitionCount = cfpTalks.size() / 10;
        List<List<CFPTalk>> partitions = Lists.partition(cfpTalks, partitionCount);

        DateTime from = day.getDate().withTime(8, 0, 0, 0);
        for (List<CFPTalk> partition : partitions) {
            from = from.plusHours(1);
            DateTime to = from.plusHours(1);
            for (CFPTalk cfpTalk : partition) {
                List<Reference<Speaker>> talkSpeakers = FluentIterable.from(cfpTalk.speakers)
                        .transform(new Function<CFPTalkSpeaker, Speaker>() {
                            @Override
                            public Speaker apply(CFPTalkSpeaker input) {
                                if (input.link != null) {
                                    return speakers.get(input.link.href);
                                }
                                return null;
                            }
                        })
                        .filter(new Predicate<Speaker>() {
                            @Override
                            public boolean apply(Speaker input) {
                                return input != null;
                            }
                        })
                        .transform(Functions.<Speaker>REFERENCER(Type.speaker))
                        .toList();
                crawlingResult.getPresentations().add(
                        (Presentation) new Presentation()
                                .setLocation(roomRef)
                                .setDay(dayRef)
                                .setEvent(eventRef)
                                .setKind(cfpTalk.talkType)
                                .setExternalId(cfpTalk.id)
                                .setTitle(cfpTalk.title)
                                .setSummary(cfpTalk.summary)
                                .setSpeakers(talkSpeakers)
                                .setFrom(from)
                                .setTo(to)
                                .setKey(new ObjectId().toString())
                );
            }
        }
    }

    protected Map<String, Speaker> getSpeakersMap(List<CFPLinks> cfpSpeakerLinks, CrawlingConfiguration configuration) throws IOException {
        Map<String, Speaker> speakers = new HashMap<>();
        for (CFPLinks cfpSpeakerLink : cfpSpeakerLinks) {
            for (CFPLink link : cfpSpeakerLink.links) {
                CFPSpeaker cfpSpeaker = MAPPER.readValue(httpGet(link.href, configuration).body(), CFPSpeaker.class);
                speakers.put(link.href, cfpSpeaker.toStdSpeaker());
            }
        }
        return speakers;
    }

    private List<CFPLinks> getCfpSpeakerLinks(List<CFPTalk> cfpTalks) {
        ImmutableList<CFPLink> cfpLinks = FluentIterable
                .from(cfpTalks)
                .filter(new Predicate<CFPTalk>() {
                    @Override
                    public boolean apply(CFPTalk input) {
                        return input.speakers != null && !input.speakers.isEmpty();
                    }
                })
                .transformAndConcat(new Function<CFPTalk, Iterable<CFPTalkSpeaker>>() {
                    @Override
                    public Iterable<CFPTalkSpeaker> apply(CFPTalk input) {
                        return input.speakers;
                    }
                })
                .transform(new Function<CFPTalkSpeaker, CFPLink>() {
                    @Override
                    public CFPLink apply(CFPTalkSpeaker input) {
                        return input.link;
                    }
                })
                .toList();
        CFPLinks links = new CFPLinks();
        links.links = cfpLinks;
        return Collections.singletonList(links);
    }
}
