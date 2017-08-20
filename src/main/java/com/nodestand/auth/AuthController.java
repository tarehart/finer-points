package com.nodestand.auth;

import com.nodestand.nodes.User;
import com.nodestand.nodes.repository.UserRepository;
import com.nodestand.service.user.UserService;
import com.nodestand.util.AliasGenerator;
import com.nodestand.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * Created by Tyler on 12/31/2016.
 */
@RestController
public class AuthController {

    public static final String
            CLIENT_ID_KEY = "client_id",
            REDIRECT_URI_KEY = "redirect_uri",
            CLIENT_SECRET = "client_secret",
            CODE_KEY = "code",
            GRANT_TYPE_KEY = "grant_type",
            AUTH_CODE = "authorization_code";

    /**
     * This gets persisted in the database, don't change it lightly.
     */
    public static final String PROVIDER_GOOGLE = "google";

    private final TokenHandler tokenHandler;
    private final UserService userService;
    private final UserRepository userRepository;
    private final AliasGenerator aliasGenerator;
    private final String googleClientSecret;
    private final RestTemplate restTemplate;

    @Autowired
    public AuthController(TokenHandler tokenHandler, UserService userService, UserRepository userRepository, AliasGenerator aliasGenerator, Environment environment) {
        googleClientSecret = environment.getProperty("googleClientSecret");
        this.tokenHandler = tokenHandler;
        this.userService = userService;
        this.userRepository = userRepository;
        this.aliasGenerator = aliasGenerator;
        this.restTemplate = new RestTemplate();
    }

    @RequestMapping("/auth/google")
    public Token loginGoogle(@RequestBody final Payload payload) {

        final String accessTokenUrl = "https://accounts.google.com/o/oauth2/token";
        final String peopleApiUrl = "https://www.googleapis.com/plus/v1/people/me/openIdConnect";

        // Step 1. Exchange authorization code for access token.
        HttpEntity<MultiValueMap<String, String>> postEntity = getAuthEntity(payload);

        ResponseEntity<GoogleAccessToken> googleAccessToken =
                restTemplate.exchange(accessTokenUrl, HttpMethod.POST, postEntity, GoogleAccessToken.class);

        // Step 2. Retrieve profile information about the current user.
        final String accessToken = googleAccessToken.getBody().access_token;

        HttpEntity<String> entity = getInfoEntity(accessToken);

        ResponseEntity<GoogleUserInfo> exchange = restTemplate.exchange(peopleApiUrl, HttpMethod.GET, entity, GoogleUserInfo.class);

        // Step 3. Process the authenticated the user.
        GoogleUserInfo googleInfo = exchange.getBody();

        final String emailAddress;
        if (Boolean.parseBoolean(googleInfo.email_verified)) {
            emailAddress = googleInfo.email;
        } else {
            emailAddress = null;
        }

        return processUser(PROVIDER_GOOGLE, googleInfo.sub, emailAddress);
    }

    private HttpEntity<String> getInfoEntity(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        return new HttpEntity<>("", headers);
    }

    private HttpEntity<MultiValueMap<String, String>> getAuthEntity(@RequestBody Payload payload) {
        final MultiValueMap<String, String> accessData = new LinkedMultiValueMap<>();
        accessData.add(CLIENT_ID_KEY, payload.getClientId());
        accessData.add(REDIRECT_URI_KEY, payload.getRedirectUri());
        accessData.add(CLIENT_SECRET, googleClientSecret);
        accessData.add(CODE_KEY, payload.getCode());
        accessData.add(GRANT_TYPE_KEY, AUTH_CODE);

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        return new HttpEntity<>(accessData, postHeaders);
    }

    public static class GoogleAccessToken {
        public String access_token;
    }

    // See https://developers.google.com/identity/sign-in/web/backend-auth
    public static class GoogleUserInfo {
        public String sub;
        public String email;
        public String email_verified;
    }

    private Token processUser(final String providerId, final String providerUserId, final String emailAddress) {

        Optional<NodeUserDetails> userDetails = userService.loadUserBySocialProvider(providerId, providerUserId);

        NodeUserDetails concreteUser;

        // Step 3b. Create a new user account or return an existing one.
        if (userDetails.isPresent()) {
            concreteUser = userDetails.get();

            if (concreteUser.getUser().getEmailAddress() == null) {
                final User user = concreteUser.getUser();
                user.setEmailAddress(emailAddress);
                userRepository.save(user);
            }

        } else {

            //add new users to the db with its default roles for later use in SocialAuthenticationSuccessHandler
            final User user = new User(
                    providerId,
                    providerUserId,
                    User.Roles.ROLE_USER);

            // Start with three aliases
            user.addNewAlias(generateUniqueUserName());
            user.addNewAlias(generateUniqueUserName());
            user.addNewAlias(generateUniqueUserName());

            user.setEmailAddress(emailAddress);

            userRepository.save(user);

            concreteUser = new NodeUserDetails(user);
        }

        return tokenHandler.generateToken(concreteUser);
    }

    private String generateUniqueUserName() {

        for (int i = 0; i < 50; i++) {
            String name = aliasGenerator.generateAlias();
            if (userRepository.findByAlias(name) == null) {
                return name;
            }
        }

        // Very bad luck!
        return IdGenerator.newId();
    }

    public static class Payload {
        String clientId;
        String redirectUri;
        String code;

        public String getClientId() {
            return clientId;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public String getCode() {
            return code;
        }
    }

}
