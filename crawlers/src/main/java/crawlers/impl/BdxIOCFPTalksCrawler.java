package crawlers.impl;

import com.google.common.collect.ImmutableList;
import restx.factory.Component;

@Component
public class BdxIOCFPTalksCrawler extends DevoxxCFPTalksCrawler {

    private static final String BASE_URL = "http://cfp.bdx.io/api/conferences/";

    public BdxIOCFPTalksCrawler() {
        super("bdxio-talks", ImmutableList.of("bdxio-publisher"), BASE_URL);
    }
}
