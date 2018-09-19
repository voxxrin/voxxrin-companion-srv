package voxxrin.companion.domain;

public class EventLinks {

    private String hashTag;

    private String twitterProfileUrl;

    private String facebookProfileUrl;

    private String websiteUrl;

    public String getHashTag() {
        return hashTag;
    }

    public EventLinks setHashTag(String hashTag) {
        this.hashTag = hashTag;
        return this;
    }

    public String getTwitterProfileUrl() {
        return twitterProfileUrl;
    }

    public EventLinks setTwitterProfileUrl(String twitterProfileUrl) {
        this.twitterProfileUrl = twitterProfileUrl;
        return this;
    }

    public String getFacebookProfileUrl() {
        return facebookProfileUrl;
    }

    public EventLinks setFacebookProfileUrl(String facebookProfileUrl) {
        this.facebookProfileUrl = facebookProfileUrl;
        return this;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public EventLinks setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
        return this;
    }
}
