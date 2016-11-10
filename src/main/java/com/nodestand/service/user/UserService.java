package com.nodestand.service.user;

import com.nodestand.auth.NodeUserDetails;
import com.nodestand.nodes.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.security.SocialUserDetailsService;

/**
 * @author mh
 * @since 08.11.11
 */
public interface UserService extends UserDetailsService, SocialUserDetailsService {

    Long getUserNodeIdFromSecurityContext();

    User getUserFromSecurityContext();

    @Override
    NodeUserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    @Override
    NodeUserDetails loadUserByUserId(String userId);

    NodeUserDetails loadUserByConnectionKey(ConnectionKey key);
}
