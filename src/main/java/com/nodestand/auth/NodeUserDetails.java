package com.nodestand.auth;

import com.nodestand.nodes.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.social.security.SocialUserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
* @author mh
* @since 07.03.11
*/
public class NodeUserDetails implements SocialUserDetails {
    private final User user;

    public NodeUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        User.Roles[] roles = user.getRole();
        if (roles ==null) return Collections.emptyList();
        return Arrays.<GrantedAuthority>asList(roles);
    }

    @Override
    public String getPassword() {
        throw new IllegalStateException("password should never be used");
    }

    @Override
    public String getUsername() {
        return user.getStableId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String getUserId() {
        return user.getStableId();
    }

    public String getProviderId() {
        return user.getProviderId();
    }

    public String getProviderUserId() {
        return user.getProviderUserId();
    }
}
