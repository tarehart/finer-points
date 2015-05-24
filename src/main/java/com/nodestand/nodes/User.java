package com.nodestand.nodes;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.security.core.GrantedAuthority;

@NodeEntity
public class User {
    @GraphId Long nodeId;

    @Indexed
    String displayName;

    @Indexed(unique = true)
    String socialId;

    String info;
    private Roles[] roles;


    public User() {
    }

    public User(String socialId, String displayName, Roles... roles) {
        this.roles = roles;
        this.displayName = displayName;
        this.socialId = socialId;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", displayName, socialId);
    }

    public Roles[] getRole() {
        return roles;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getInfo() {
        return info;
    }
    public void setInfo(String info) {
        this.info = info;
    }

    public String getSocialId() {
        return socialId;
    }

    public enum Roles implements GrantedAuthority {
        ROLE_USER, ROLE_ADMIN;

        @Override
        public String getAuthority() {
            return name();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;
        if (nodeId == null) return super.equals(o);
        return nodeId.equals(user.nodeId);

    }

    public Long getNodeId() {
        return nodeId;
    }

    @Override
    public int hashCode() {

        return nodeId != null ? nodeId.hashCode() : super.hashCode();
    }
}
