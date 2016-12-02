package com.nodestand.auth;

import com.nodestand.service.user.UserService;
import org.springframework.security.core.AuthenticationException;
import org.springframework.social.connect.*;
import org.springframework.social.connect.mem.TemporaryConnectionRepository;

import java.util.*;

public class SimpleUsersConnectionRepository implements UsersConnectionRepository {

    private UserService userService;

    private ConnectionFactoryLocator connectionFactoryLocator;
    private ConnectionSignUp connectionSignUp;

    public SimpleUsersConnectionRepository(UserService userService, ConnectionFactoryLocator connectionFactoryLocator) {
        this.userService = userService;
        this.connectionFactoryLocator = connectionFactoryLocator;
    }

    @Override
    public List<String> findUserIdsWithConnection(Connection<?> connection) {
        try {
            NodeUserDetails user = userService.loadUserByConnectionKey(connection.getKey());
            return Collections.singletonList(user.getUserId());
        } catch (AuthenticationException ae) {
            return Collections.singletonList(connectionSignUp.execute(connection));
        }
    }

    @Override
    public Set<String> findUserIdsConnectedTo(String providerId, Set<String> providerUserIds) {
        Set<String> keys = new HashSet<>();
        for (String userId: providerUserIds) {
            ConnectionKey ck = new ConnectionKey(providerId, userId);
            try {
                keys.add(userService.loadUserByConnectionKey(ck).getUserId());
            } catch (AuthenticationException ae) {
                //ignore
            }
        }
        return keys;
    }

    @Override
    public ConnectionRepository createConnectionRepository(String userId) {
        final ConnectionRepository connectionRepository = new TemporaryConnectionRepository(connectionFactoryLocator);
        final NodeUserDetails user = userService.loadUserByUserId(userId);
        final ConnectionData connectionData = new ConnectionData(
                user.getProviderId(),
                user.getProviderUserId(),
                null, null, null, null, null, null, null);

        final Connection connection = connectionFactoryLocator
                .getConnectionFactory(user.getProviderId()).createConnection(connectionData);
        connectionRepository.addConnection(connection);
        return connectionRepository;
    }

    public void setConnectionSignUp(ConnectionSignUp connectionSignUp) {
        this.connectionSignUp = connectionSignUp;
    }
}
