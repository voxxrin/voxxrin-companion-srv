package voxxrin.companion.utils;

import com.google.common.base.Function;
import voxxrin.companion.auth.OAuthProvider;
import voxxrin.companion.domain.technical.Referenceable;

public class Functions {

    public static Function<OAuthProvider, String> OAUTH_PROVIDER_MAP_INDEXER = new Function<OAuthProvider, String>() {
        @Override
        public String apply(OAuthProvider input) {
            return input.getProvider();
        }
    };

    public static Function<Referenceable, String> KEY_SUPPLIER = new Function<Referenceable, String>() {
        @Override
        public String apply(Referenceable input) {
            return input.getKey();
        }
    };
}
