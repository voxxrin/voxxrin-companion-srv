package crawlers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import crawlers.configuration.CrawlingConfiguration;
import org.joda.time.DateTime;
import voxxrin.companion.domain.Day;
import voxxrin.companion.domain.Presentation;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public abstract class AbstractHttpCrawler {

    { configureMapper(MAPPER); }

    protected static final ObjectMapper MAPPER = new ObjectMapper();

    private final String id;

    @JsonIgnore
    private final List<String> roles;

    public AbstractHttpCrawler(String id, List<String> roles) {
        this.id = id;
        this.roles = concatWithAdminRoles(roles);
    }

    private List<String> concatWithAdminRoles(List<String> roles) {
        return ImmutableList.<String>builder()
                .addAll(roles)
                .add("ADM")
                .add("restx-admin")
                .build();
    }

    public abstract CrawlingResult crawl(CrawlingConfiguration configuration) throws IOException;

    public CrawlingResult setup(CrawlingResult result, CrawlingConfiguration configuration) {
        if (configuration.getEventName() != null) {
            result.getEvent().setName(configuration.getEventName());
        }
        result.getEvent().setEventId(configuration.getEventId());
        Collections.sort(result.getDays(), new Comparator<Day>() {
            @Override
            public int compare(Day o1, Day o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
        int index = 1;
        for (Day day : result.getDays()) {
            day.setExternalId(result.getEvent().getEventId() + "-" + (index++));
        }
        for (Presentation presentation : result.getPresentations()) {
            if (Strings.isNullOrEmpty(presentation.getExternalId())) {
                presentation.setExternalId(UUID.randomUUID().toString());
            }
        }
        return result;
    }

    protected void configureMapper(ObjectMapper mapper) {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JodaModule());
    }

    protected CollectionType buildCollectionType(Class<?> clazz) {
        return MAPPER.getTypeFactory().constructCollectionType(List.class, clazz);
    }

    public String getId() {
        return id;
    }

    public List<String> getRoles() {
        return roles;
    }

    protected void setEventTemporalLimits(CrawlingResult crawlingResult) {

        DateTime from = null;
        DateTime to = null;

        for (final Presentation presentation : crawlingResult.getPresentations()) {

            if (from == null) {
                from = presentation.getFrom();
            }
            if (to == null) {
                to = presentation.getTo();
            }
            if (presentation.getFrom().isBefore(from)) {
                from = presentation.getFrom();
            }
            if (presentation.getTo().isAfter(from)) {
                to = presentation.getTo();
            }

            Optional<Day> linkedDay = Iterables.tryFind(crawlingResult.getDays(), new Predicate<Day>() {
                @Override
                public boolean apply(Day input) {
                    return input.getKey().equals(presentation.getDay().getUri().getKey());
                }
            });
            if (linkedDay.isPresent() && linkedDay.get().getDate() == null) {
                linkedDay.get().setDate(presentation.getFrom().withTimeAtStartOfDay());
            }
        }

        crawlingResult.getEvent()
                .setFrom(from)
                .setTo(to);
    }
}
