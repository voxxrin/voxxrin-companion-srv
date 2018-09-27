package voxxrin.companion.auth.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Optional;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import restx.factory.Component;
import restx.jackson.FrontObjectMapperFactory;
import voxxrin.companion.auth.OAuthProvider;
import voxxrin.companion.auth.OAuthSettings;
import voxxrin.companion.domain.User;

import javax.inject.Named;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class GoogleOAuthProvider extends OAuthProvider {

    private static final Logger logger = getLogger(GoogleOAuthProvider.class);

    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String API_CALLBACK_URL = "/api/auth/provider/google";
    private static final String GOOGLE_TOKEN_URL = "https://www.googleapis.com/oauth2/v4/token";
    private static final String GOOGLE_PROFILE_URL = "https://www.googleapis.com/userinfo/v2/me";

    private final ObjectMapper mapper;
    private final String serverUrl;
    private final String appKey;
    private final String appSecret;

    public GoogleOAuthProvider(@Named("restx.server.baseUrl") String serverUrl,
                               OAuthSettings oAuthSettings,
                               @Named(FrontObjectMapperFactory.MAPPER_NAME) ObjectMapper mapper) {
        super("google", null);
        this.serverUrl = serverUrl;
        this.appKey = oAuthSettings.oauthGoogleAppId();
        this.appSecret = oAuthSettings.oauthGoogleAppSecret();
        this.mapper = mapper;
        logger.info("Registered Google provider - key = {}, secret = {}, callback = {}", appKey, appSecret, serverUrl + API_CALLBACK_URL);
    }

    @Override
    public String getProviderUrl() {
        return String.format("%s?client_id=%s&redirect_uri=%s&scope=%s&response_type=%s",
                GOOGLE_AUTH_URL, appKey, encodeUrl(serverUrl + API_CALLBACK_URL), "profile", "code");
    }

    @Override
    public <T extends Map<String, ?>> Optional<User> authenticate(Optional<T> params) throws IOException {

        Map<String, List<String>> castedParams = castParams(params);
        Optional<String> code = extractFirstParam(castedParams.get("code"));

        String tokenRequestBody = String.format("client_id=%s&client_secret=%s&code=%s&redirect_uri=%s&grant_type=authorization_code",
                appKey, appSecret, code.get(), encodeUrl(serverUrl + API_CALLBACK_URL));

        String tokenJsonResponse = HttpRequest.post(GOOGLE_TOKEN_URL)
                .send(tokenRequestBody)
                .trustAllHosts()
                .trustAllCerts()
                .body();

        JsonNode tokenTreeResponse = mapper.readTree(tokenJsonResponse);

        String profileBody = HttpRequest.get(GOOGLE_PROFILE_URL)
                .header("Authorization", String.format("Bearer %s", tokenTreeResponse.get("access_token")))
                .body();

        JsonNode profileNode = mapper.readTree(profileBody);

        User user = new User()
                .setDisplayName(profileNode.get("name").asText())
                .setFirstName(profileNode.get("given_name").asText())
                .setLastName(profileNode.get("family_name").asText())
                .setAvatarUrl(profileNode.get("picture").asText())
                .setLogin("google:" + profileNode.get("id").asText())
                .setId(new ObjectId().toString());

        logger.info("logged user is {}", user.getDisplayName());

        return Optional.of(user);
    }
}
