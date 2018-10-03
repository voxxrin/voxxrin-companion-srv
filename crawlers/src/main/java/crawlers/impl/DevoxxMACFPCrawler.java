package crawlers.impl;

import com.google.common.collect.ImmutableList;
import restx.factory.Component;

@Component
public class DevoxxMACFPCrawler extends DevoxxCFPCrawler {

    private static final String BASE_URL = "http://cfp.devoxx.ma/api/conferences/";

    public DevoxxMACFPCrawler() {
        super("devoxxma", ImmutableList.of("devoxxma-publisher"), BASE_URL);
    }
}
