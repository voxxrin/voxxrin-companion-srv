package voxxrin.companion.auth.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
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
import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class GithubOAuthProvider extends OAuthProvider {

    private static final Logger logger = getLogger(GithubOAuthProvider.class);

    private static final String GITHUB_AUTH_URL = "https://github.com/login/oauth/authorize";
    private static final String API_CALLBACK_URL = "/api/auth/provider/github";
    private static final String GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String GITHUB_PROFILE_URL = "https://api.github.com/user";

    private final ObjectMapper mapper;
    private final String serverUrl;
    private final String appKey;
    private final String appSecret;

    public GithubOAuthProvider(@Named("restx.server.baseUrl") String serverUrl,
                               OAuthSettings oAuthSettings,
                               @Named(FrontObjectMapperFactory.MAPPER_NAME) ObjectMapper mapper) {
        super("github", null);
        this.serverUrl = serverUrl;
        this.appKey = oAuthSettings.oauthGithubAppId();
        this.appSecret = oAuthSettings.oauthGithubAppSecret();
        this.mapper = mapper;
        logger.info("Registered Github provider - key = {}, secret = {}, callback = {}", appKey, appSecret, serverUrl + API_CALLBACK_URL);
    }

    @Override
    public String getProviderUrl() {
        return String.format("%s?client_id=%s&redirect_uri=%s&scope=%s&state=%s",
                GITHUB_AUTH_URL, appKey, encodeUrl(serverUrl + API_CALLBACK_URL), "user",
                UUID.randomUUID().toString());
    }

    @Override
    public <T extends Map<String, ?>> Optional<User> authenticate(Optional<T> params) throws IOException {

        Map<String, List<String>> castedParams = castParams(params);
        Optional<String> code = extractFirstParam(castedParams.get("code"));

        HttpRequest obtainTokenRequest = HttpRequest.get(String.format("%s?client_id=%s&client_secret=%s&code=%s", GITHUB_TOKEN_URL, appKey, appSecret, code.get()))
                .trustAllHosts()
                .trustAllCerts();

        Map<String, String> tokenChain = Splitter.on('&').withKeyValueSeparator('=').split(obtainTokenRequest.body());
        String accessToken = tokenChain.get("access_token");

        String profileBody = HttpRequest.get(GITHUB_PROFILE_URL)
                .header("Authorization", String.format("token %s", accessToken))
                .body();

        Map<String, Object> providerInfo = mapper.readValue(profileBody, buildStringParamsMapType());

        String login = (String) providerInfo.get("login");
        String displayName = Optional.fromNullable((String) providerInfo.get("name")).or(login);
        String emailAddress = (String) providerInfo.get("email");
        String pictureUrl = (String) providerInfo.get("avatar_url");

        User user = new User()
                .setEmailAddress(emailAddress)
                .setDisplayName(displayName)
                .setAvatarUrl(pictureUrl)
                .setLogin("github:" + login)
                .setId(new ObjectId().toString());

        logger.info("logged user is {}", user.getDisplayName());

        return Optional.of(user);
    }

    private MapType buildStringParamsMapType() {
        return TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class);
    }
}
