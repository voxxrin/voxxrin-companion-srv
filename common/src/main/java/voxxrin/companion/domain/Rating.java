package voxxrin.companion.domain;

import org.joda.time.DateTime;

import java.util.Set;
import java.util.TreeSet;

public class Rating implements HasPresentationRef<Rating> {

    public static final String COLLECTION = "rating";

    private String userId;

    private String presentationRef;

    private Set<RatingItem> ratingItems = new TreeSet<>();

    private DateTime dateTime;

    public String getUserId() {
        return userId;
    }

    public Rating setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public String getPresentationRef() {
        return presentationRef;
    }

    @Override
    public Rating setPresentationRef(String presentationRef) {
        this.presentationRef = presentationRef;
        return this;
    }

    public Set<RatingItem> getRatingItems() {
        return ratingItems;
    }

    public Rating setRatingItems(Set<RatingItem> ratingItems) {
        this.ratingItems = ratingItems;
        return this;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public Rating setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
        return this;
    }
}
