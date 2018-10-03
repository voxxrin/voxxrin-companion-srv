package voxxrin.companion.services;

import com.google.common.collect.Lists;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import restx.factory.Component;
import voxxrin.companion.domain.Presentation;
import voxxrin.companion.domain.Subscription;

import static org.slf4j.LoggerFactory.getLogger;
import static voxxrin.companion.utils.PresentationRef.buildPresentationBusinessRef;

@Component
public class PushService {

    private static final Logger logger = getLogger(PushService.class);
    private FirebaseMessaging firebaseMessaging;

    public PushService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    private String buildTalkBeginningTopicName(String presentationRef) {
        return String.format("presentation.%s.beginning", presentationRef.replaceAll("[:/]", "_"));
    }

    private String buildTalkContentTopicName(String presentationRef) {
        return String.format("presentation.%s.content", presentationRef.replaceAll("[:/]", "_"));
    }

    public void publishTalkBeginning(Presentation presentation) {

        String presentationRef = buildPresentationBusinessRef(presentation);
        String topicName = buildTalkBeginningTopicName(presentationRef);
        Notification notification = new Notification("Le talk va commencer", presentation.getTitle());
        logger.info("publishing talk beginning {}", topicName);

        Message message = Message.builder().setTopic(topicName).setNotification(notification).build();
        firebaseMessaging.sendAsync(message);
    }

    public void publishTalkContent(Presentation presentation) {

        String presentationRef = buildPresentationBusinessRef(presentation);
        String topicName = buildTalkContentTopicName(presentationRef);
        Notification notification = new Notification("Du contenu a été publié", presentation.getTitle());
        logger.info("publishing talk content {}", topicName);

        Message message = Message.builder().setTopic(topicName).setNotification(notification).build();
        firebaseMessaging.sendAsync(message);
    }

    private void subscribeToTopic(String topicName, String deviceToken) {
        logger.info("subscribing to topic {}", topicName);
        firebaseMessaging.subscribeToTopicAsync(Lists.newArrayList(deviceToken), topicName);
    }

    private void unsubscribeFromTopic(String topicName, String deviceToken) {
        logger.info("unsubscribing from topic {}", topicName);
        firebaseMessaging.unsubscribeFromTopicAsync(Lists.newArrayList(deviceToken), topicName);
    }

    public void subscribeToTalkBeginning(Subscription subscription, String deviceToken) {
        subscribeToTopic(buildTalkBeginningTopicName(subscription.getPresentationRef()), deviceToken);
    }

    public void unsubscribeFromTalkBeginning(Subscription subscription, String deviceToken) {
        String topicName = buildTalkBeginningTopicName(subscription.getPresentationRef());
        unsubscribeFromTopic(topicName, deviceToken);
    }

    public void subscribeToTalkContent(Subscription subscription, String deviceToken) {
        String topicName = buildTalkContentTopicName(subscription.getPresentationRef());
        subscribeToTopic(topicName, deviceToken);
    }

    public void unsubscribeFromTalkContent(Subscription subscription, String deviceToken) {
        String topicName = buildTalkContentTopicName(subscription.getPresentationRef());
        unsubscribeFromTopic(topicName, deviceToken);
    }
}
