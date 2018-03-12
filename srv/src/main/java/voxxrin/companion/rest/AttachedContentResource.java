package voxxrin.companion.rest;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import restx.RestxRequest;
import restx.annotations.POST;
import restx.annotations.Param;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.RestxSecurityManager;
import voxxrin.companion.auth.AuthModule;
import voxxrin.companion.domain.AttachedContent;
import voxxrin.companion.domain.Presentation;
import voxxrin.companion.domain.Subscription;
import voxxrin.companion.domain.Type;
import voxxrin.companion.domain.technical.Reference;
import voxxrin.companion.persistence.PresentationsDataService;
import voxxrin.companion.persistence.RemindersService;
import voxxrin.companion.auth.AuthModule;
import voxxrin.companion.domain.AttachedContent;
import voxxrin.companion.domain.Presentation;
import voxxrin.companion.domain.Subscription;
import voxxrin.companion.domain.Type;
import voxxrin.companion.domain.technical.Reference;
import voxxrin.companion.persistence.PresentationsDataService;
import voxxrin.companion.persistence.RemindersService;
import voxxrin.companion.security.Permissions;
import voxxrin.companion.services.PushService;

@Component
@RestxResource
public class AttachedContentResource {

    private final PushService push;
    private final PresentationsDataService presentationsDataService;
    private final RemindersService remindersService;
    private final RestxSecurityManager securityManager;

    public AttachedContentResource(PushService push,
                                   PresentationsDataService presentationsDataService,
                                   RemindersService remindersService,
                                   RestxSecurityManager securityManager) {
        this.push = push;
        this.presentationsDataService = presentationsDataService;
        this.remindersService = remindersService;
        this.securityManager = securityManager;
    }

    @POST("/presentation/{presentationId}/attachedContent")
    public AttachedContent attachContentToPresentation(@Param(kind = Param.Kind.PATH) String presentationId,
                                                       @Param(kind = Param.Kind.BODY) AttachedContent content,
                                                       @Param(kind = Param.Kind.CONTEXT) RestxRequest request) {

        Presentation presentation = Reference.<Presentation>of(Type.presentation, presentationId).get();
        if (presentation == null) {
            return null;
        }

        securityManager.check(request, Permissions.buildEventAdminPermission(presentation.getEvent().get()));

        Iterable<Subscription> subs = remindersService.getReminders(presentation);
        content.setUserId(AuthModule.currentUser().get().getId());
        presentationsDataService.attachReleasedContent(presentation, content);

        ImmutableList<String> userIds = toUserIds(subs);
        push.sendReleasedContentNotification(presentation, userIds);

        return content;
    }

    private ImmutableList<String> toUserIds(Iterable<Subscription> subs) {
        return FluentIterable
                .from(subs)
                .transform(new Function<Subscription, String>() {
                    @Override
                    public String apply(Subscription input) {
                        return input.getUserId();
                    }
                })
                .toList();
    }
}
