package com.nodestand.nodes;

import com.nodestand.service.PasswordEncoderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.annotation.*;
import org.springframework.security.core.GrantedAuthority;

@NodeEntity
public class User {
    @GraphId Long nodeId;

    @Indexed(unique = true)
    String login;

    String name;
    String password;
    String info;
    private Roles[] roles;

    public User() {
    }

    public User(String login, String name, String password, PasswordEncoderService passwordService, Roles... roles) {
        this.login = login;
        this.name = name;
        this.password = passwordService.encodePassword(password);
        this.roles = roles;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", name, login);
    }

    public String getName() {
        return name;
    }

    public Roles[] getRole() {
        return roles;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getInfo() {
        return info;
    }
    public void setInfo(String info) {
        this.info = info;
    }

    public void updatePassword(String old, String newPass1, String newPass2, PasswordEncoderService passwordService) {
        if (!password.equals(passwordService.encodePassword(old))) throw new IllegalArgumentException("Existing Password invalid");
        if (!newPass1.equals(newPass2)) throw new IllegalArgumentException("New Passwords don't match");
        this.password = passwordService.encodePassword(newPass1);
    }

    public void setName(String name) {
        this.name = name;
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

    public Long getId() {
        return nodeId;
    }

    @Override
    public int hashCode() {

        return nodeId != null ? nodeId.hashCode() : super.hashCode();
    }
}
