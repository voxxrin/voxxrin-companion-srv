package voxxrin.companion.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.io.Resources;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.slf4j.Logger;
import restx.factory.Module;
import restx.factory.Provides;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import static org.slf4j.LoggerFactory.getLogger;

@Module
public class PushModule {

    private static final Logger logger = getLogger(PushModule.class);
    private static final String DB_URL = "https://voxxrin3.firebaseio.com";

    private GoogleCredentials credentials() {
        try {
            URL resource = Resources.getResource("firebase/voxxrin3-firebase.json");
            return GoogleCredentials.fromStream(new FileInputStream(new File(resource.getFile())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    public FirebaseMessaging firebaseMessaging() {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(credentials())
                .setDatabaseUrl(DB_URL)
                .build();
        FirebaseApp.initializeApp(options);
        logger.info("[PUSH] - connection initialized to firebase @ {}", DB_URL);
        return FirebaseMessaging.getInstance();
    }
}
