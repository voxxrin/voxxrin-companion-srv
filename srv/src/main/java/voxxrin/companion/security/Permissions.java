package voxxrin.companion.security;

import com.google.common.base.Optional;
import restx.RestxRequest;
import restx.security.Permission;
import restx.security.RestxPrincipal;
import voxxrin.companion.domain.Event;
import voxxrin.companion.domain.User;
import voxxrin.companion.domain.Event;
import voxxrin.companion.domain.User;

public class Permissions {

    public static Permission buildEventAdminPermission(final Event event) {
        return new Permission() {
            @Override
            public Optional<? extends Permission> has(RestxPrincipal principal, RestxRequest request) {

                User user = (User) principal;
                if (user.isAdmin()) {
                    return Optional.of(this);
                }

                String role = event.getEventId() + "-admin";
                if (user.getPrincipalRoles().contains(role)) {
                    return Optional.of(this);
                }

                return Optional.absent();
            }
        };
    }
}
