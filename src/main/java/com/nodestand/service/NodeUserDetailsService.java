package com.nodestand.service;

import com.nodestand.nodes.User;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author mh
 * @since 08.11.11
 */
public interface NodeUserDetailsService extends UserDetailsService {
    @Override
    NodeUserDetails loadUserByUsername(String login) throws UsernameNotFoundException, DataAccessException;

    User getUserFromSession();

    @Transactional
    User register(String login, String name, String password);

}
