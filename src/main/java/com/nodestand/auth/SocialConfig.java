package com.nodestand.auth;

import com.nodestand.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.social.UserIdSource;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurerAdapter;
import org.springframework.social.connect.*;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.social.google.api.Google;
import org.springframework.social.google.connect.GoogleConnectionFactory;
import org.springframework.social.security.SocialAuthenticationServiceRegistry;

/**
 * Spring Social Configuration.
 * https://github.com/GabiAxel/spring-social-google-example
 *
 * Also adapting https://github.com/Robbert1/boot-stateless-social/blob/master/src/main/java/com/jdriven/stateless/security/StatelessSocialConfig.java
 */
@Configuration
@EnableSocial
public class SocialConfig extends SocialConfigurerAdapter {

    @Autowired
    private Environment environment;

    @Autowired
    private UserService userDetailsService;

    @Autowired
    private ConnectionSignUp autoSignUpHandler;

    @Override
    public UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {

        SimpleUsersConnectionRepository usersConnectionRepository =
                new SimpleUsersConnectionRepository(userDetailsService, connectionFactoryLocator);

        // if no local user record exists yet for a facebook's user id
        // automatically create a User and add it to the database
        usersConnectionRepository.setConnectionSignUp(autoSignUpHandler);

        return usersConnectionRepository;
    }

    /**
     * When a new provider is added to the app, register its {@link ConnectionFactory} here.
     * @see GoogleConnectionFactory
     */
    @Bean
    public SocialAuthenticationServiceRegistry connectionFactoryLocator() {
        SocialAuthenticationServiceRegistry registry = new SocialAuthenticationServiceRegistry();
        // Create an application.properties file in the project root and fill in these properties from
        // https://console.developers.google.com/project/node-stand/apiui/credential
        registry.addConnectionFactory(new GoogleConnectionFactory(environment.getProperty("googleClientId"),
                environment.getProperty("googleClientSecret")));
        return registry;
    }

    /**
     * A proxy to a request-scoped object representing the current user's primary Google account.
     * @throws NotConnectedException if the user is not connected to facebook.
     */
    @Bean
    @Scope(value="request", proxyMode=ScopedProxyMode.INTERFACES)
    public Google google(ConnectionRepository repository) {
        return repository.getPrimaryConnection(Google.class).getApi();
    }

    @Override
    public UserIdSource getUserIdSource() {
        // retrieve the UserId from the UserAuthentication in the security context
        return () -> {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            NodeUserDetails user = null;
            if (authentication instanceof UsernamePasswordAuthenticationToken) {
                user = (NodeUserDetails) authentication.getPrincipal();
            }

            if (user == null) {
                throw new IllegalStateException("Unable to get a ConnectionRepository: no user signed in");
            }
            return user.getUserId();
        };
    }

    @Bean
    public SignInAdapter signInAdapter() {
        return new ImplicitSignInAdapter(new HttpSessionRequestCache());
    }

}
