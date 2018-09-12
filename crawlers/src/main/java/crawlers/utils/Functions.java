package crawlers.utils;

import com.google.common.base.Function;
import crawlers.AbstractHttpCrawler;
import voxxrin.companion.domain.Type;
import voxxrin.companion.domain.technical.Reference;
import voxxrin.companion.domain.technical.Referenceable;

public class Functions {

    public static Function<AbstractHttpCrawler, String> CRAWLERS_MAP_INDEXER = new Function<AbstractHttpCrawler, String>() {
        @Override
        public String apply(AbstractHttpCrawler input) {
            return input.getId();
        }
    };

    public static <T extends Referenceable> Function<T, Reference<T>> REFERENCER(final Type type) {
        return new Function<T, Reference<T>>() {
            @Override
            public Reference<T> apply(T input) {
                return Reference.of(type, input.getKey());
            }
        };
    }
}
