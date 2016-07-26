package org.springframework.social.connect.mem;

import org.springframework.social.connect.ConnectionFactoryLocator;

public class TemporaryConnectionRepository extends InMemoryConnectionRepository {
    public TemporaryConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
        super(connectionFactoryLocator);
    }
}
