package voxxrin.companion.domain;

import org.joda.time.DateTime;

public class Subscription implements HasPresentationRef<Subscription> {

    public static final String FAVORITES_COLLECTION = "favorite";
    public static final String REMINDERS_COLLECTION = "remindMe";

    private String presentationRef;

    private String userId;

    private DateTime dateTime = DateTime.now();

    @Override
    public String getPresentationRef() {
        return presentationRef;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public Subscription setPresentationRef(final String presentationRef) {
        this.presentationRef = presentationRef;
        return this;
    }

    public Subscription setUserId(final String userId) {
        this.userId = userId;
        return this;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public Subscription setDateTime(final DateTime dateTime) {
        this.dateTime = dateTime;
        return this;
    }
}
