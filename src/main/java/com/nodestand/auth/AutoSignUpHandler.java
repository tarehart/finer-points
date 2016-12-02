package com.nodestand.auth;

import com.nodestand.nodes.User;
import com.nodestand.nodes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AutoSignUpHandler implements ConnectionSignUp {

    private final UserRepository userRepository;

    private volatile long userCount;

    @Autowired
    public AutoSignUpHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public String execute(final Connection<?> connection) {
        //add new users to the db with its default roles for later use in SocialAuthenticationSuccessHandler
        final User user = new User(
                connection.getKey().getProviderId(),
                connection.getKey().getProviderUserId(),
                generateUniqueUserName(connection.fetchUserProfile().getFirstName()),
                User.Roles.ROLE_USER);

        userRepository.save(user);
        return user.getStableId();
    }


    private String generateUniqueUserName(final String firstName) {
        String username = getUsernameFromFirstName(firstName);
        String option = username;
        for (int i = 0; userRepository.findByUsername(option) != null; i++) {
            option = username + i;
        }
        return option;
    }

    private String getUsernameFromFirstName(final String userId) {
        final int max = 25;
        int index = userId.indexOf(' ');
        index = index == -1 || index > max ? userId.length() : index;
        index = index > max ? max : index;
        return userId.substring(0, index);
    }
}
