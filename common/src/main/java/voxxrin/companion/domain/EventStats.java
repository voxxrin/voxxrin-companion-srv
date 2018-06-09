package voxxrin.companion.domain;

import java.util.HashMap;
import java.util.Map;

public class EventStats {

    /**
     * Event
     */

    private String eventId;

    private String eventName;

    private int talksCount;

    private int speakersCount;

    /**
     * Subscriptions
     */

    private int favoritesCount;

    private int usersWithFavoritesCount;

    private int remindersCount;

    private int usersWithRemindersCount;

    private Presentation topFavoritedPresentation;

    private Presentation topRemindedPresentation;

    /**
     * Ratings
     */

    private Map<String, Presentation> topRatings = new HashMap<>();

    public String getEventId() {
        return eventId;
    }

    public EventStats setEventId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    public String getEventName() {
        return eventName;
    }

    public EventStats setEventName(String eventName) {
        this.eventName = eventName;
        return this;
    }

    public int getTalksCount() {
        return talksCount;
    }

    public EventStats setTalksCount(int talksCount) {
        this.talksCount = talksCount;
        return this;
    }

    public int getSpeakersCount() {
        return speakersCount;
    }

    public EventStats setSpeakersCount(int speakersCount) {
        this.speakersCount = speakersCount;
        return this;
    }

    public int getFavoritesCount() {
        return favoritesCount;
    }

    public EventStats setFavoritesCount(int favoritesCount) {
        this.favoritesCount = favoritesCount;
        return this;
    }

    public int getUsersWithFavoritesCount() {
        return usersWithFavoritesCount;
    }

    public EventStats setUsersWithFavoritesCount(int usersWithFavoritesCount) {
        this.usersWithFavoritesCount = usersWithFavoritesCount;
        return this;
    }

    public int getRemindersCount() {
        return remindersCount;
    }

    public EventStats setRemindersCount(int remindersCount) {
        this.remindersCount = remindersCount;
        return this;
    }

    public int getUsersWithRemindersCount() {
        return usersWithRemindersCount;
    }

    public EventStats setUsersWithRemindersCount(int usersWithRemindersCount) {
        this.usersWithRemindersCount = usersWithRemindersCount;
        return this;
    }

    public Presentation getTopFavoritedPresentation() {
        return topFavoritedPresentation;
    }

    public EventStats setTopFavoritedPresentation(Presentation topFavoritedPresentation) {
        this.topFavoritedPresentation = topFavoritedPresentation;
        return this;
    }

    public Presentation getTopRemindedPresentation() {
        return topRemindedPresentation;
    }

    public EventStats setTopRemindedPresentation(Presentation topRemindedPresentation) {
        this.topRemindedPresentation = topRemindedPresentation;
        return this;
    }

    public Map<String, Presentation> getTopRatings() {
        return topRatings;
    }

    public EventStats setTopRatings(Map<String, Presentation> topRatings) {
        this.topRatings = topRatings;
        return this;
    }
}
