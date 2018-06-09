package voxxrin.companion.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RatingItem {

    public static final String COLLECTION = "ratingItem";

    private String key;

    private Map<String, String> labels = new HashMap<>();

    public String getKey() {
        return key;
    }

    public RatingItem setKey(String key) {
        this.key = key;
        return this;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public RatingItem setLabels(Map<String, String> labels) {
        this.labels = labels;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RatingItem that = (RatingItem) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {

        return Objects.hash(key);
    }
}
