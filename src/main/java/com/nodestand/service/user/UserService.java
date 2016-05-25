package com.nodestand.service.user;

import com.nodestand.auth.NodeUserDetails;
import com.nodestand.nodes.User;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author mh
 * @since 08.11.11
 */
public interface UserService extends UserDetailsService {
    void setCurrentUser(User user);

    @Override
    NodeUserDetails loadUserByUsername(String socialId) throws UsernameNotFoundException, DataAccessException;

    Long getUserIdFromSession();

    String getSocialIdFromSession();

    @Transactional
    NodeUserDetails register(String socialId, String login);

    User getProfile(String stableId);
}
