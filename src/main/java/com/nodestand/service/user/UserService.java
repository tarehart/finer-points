package com.nodestand.service.user;

import com.nodestand.auth.NodeUserDetails;
import com.nodestand.nodes.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

/**
 * @author mh
 * @since 08.11.11
 */
public interface UserService extends UserDetailsService {

    Long getUserNodeIdFromSecurityContext();

    User getUserFromSecurityContext();

    @Override
    NodeUserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    NodeUserDetails loadUserByUserId(String userId);

    Optional<NodeUserDetails> loadUserBySocialProvider(String providerId, String providerUserId);

    User loadUserWithVotes(String stableId);
}
