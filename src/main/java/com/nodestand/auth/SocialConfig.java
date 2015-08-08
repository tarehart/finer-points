package com.nodestand.auth;

import com.nodestand.nodes.User;
import com.nodestand.service.NodeUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurerAdapter;
import org.springframework.social.connect.*;
import org.springframework.social.connect.mem.InMemoryUsersConnectionRepository;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.connect.web.ProviderSignInController;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.social.google.api.Google;
import org.springframework.social.google.connect.GoogleConnectionFactory;

/**
* Spring Social Configuration.
* https://github.com/GabiAxel/spring-social-google-example
* @author Keith Donald
*/
@Configuration
@EnableSocial
public class SocialConfig extends SocialConfigurerAdapter {

    @Autowired
    private Environment environment;

    @Autowired
    private NodeUserDetailsService userDetailsService;

    @Override
    public UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
        InMemoryUsersConnectionRepository repository = new InMemoryUsersConnectionRepository(
                connectionFactoryLocator());
        repository.setConnectionSignUp(new ImplicitConnectionSignup());
        return repository;
    }

    /**
     * When a new provider is added to the app, register its {@link ConnectionFactory} here.
     * @see GoogleConnectionFactory
     */
    @Bean
    public ConnectionFactoryLocator connectionFactoryLocator() {
        ConnectionFactoryRegistry registry = new ConnectionFactoryRegistry();
        // Create an application.properties file in the project root and fill in these properties from
        // https://console.developers.google.com/project/node-stand/apiui/credential
        registry.addConnectionFactory(new GoogleConnectionFactory(environment.getProperty("googleClientId"),
                environment.getProperty("googleClientSecret")));
        return registry;
    }

    /**
     * Request-scoped data access object providing access to the current user's connections.
     */
    @Bean
    @Scope(value="request", proxyMode=ScopedProxyMode.INTERFACES)
    public ConnectionRepository connectionRepository() {
        User user = userDetailsService.getUserFromSession();
        return getUsersConnectionRepository(connectionFactoryLocator()).createConnectionRepository(user.getSocialId());
    }

    /**
     * A proxy to a request-scoped object representing the current user's primary Google account.
     * @throws NotConnectedException if the user is not connected to facebook.
     */
    @Bean
    @Scope(value="request", proxyMode=ScopedProxyMode.INTERFACES)
    public Google google() {
        return connectionRepository().getPrimaryConnection(Google.class).getApi();
    }

    private static class ImplicitConnectionSignup implements ConnectionSignUp {
        @Override
        public String execute(Connection<?> connection) {
            return connection.getKey().getProviderUserId();
        }
    }

    @Bean
    public SignInAdapter signInAdapter() {
        return new ImplicitSignInAdapter(new HttpSessionRequestCache());
    }

}
