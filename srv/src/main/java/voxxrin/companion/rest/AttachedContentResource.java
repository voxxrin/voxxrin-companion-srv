package voxxrin.companion.rest;

import restx.RestxRequest;
import restx.annotations.POST;
import restx.annotations.Param;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.RestxSecurityManager;
import voxxrin.companion.auth.AuthModule;
import voxxrin.companion.domain.AttachedContent;
import voxxrin.companion.domain.Presentation;
import voxxrin.companion.domain.Type;
import voxxrin.companion.domain.technical.Reference;
import voxxrin.companion.persistence.PresentationsDataService;
import voxxrin.companion.security.Permissions;
import voxxrin.companion.services.PushService;

@Component
@RestxResource
public class AttachedContentResource {

    private final PushService pushService;
    private final PresentationsDataService presentationsDataService;
    private final RestxSecurityManager securityManager;

    public AttachedContentResource(PushService pushService,
                                   PresentationsDataService presentationsDataService,
                                   RestxSecurityManager securityManager) {
        this.pushService = pushService;
        this.presentationsDataService = presentationsDataService;
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

        content.setUserId(AuthModule.currentUser().get().getId());
        presentationsDataService.attachReleasedContent(presentation, content);

        pushService.publishTalkContent(presentation);

        return content;
    }
}
