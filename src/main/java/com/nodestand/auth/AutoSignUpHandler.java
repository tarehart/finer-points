package com.nodestand.auth;

import com.nodestand.nodes.User;
import com.nodestand.nodes.repository.UserRepository;
import com.nodestand.util.AliasGenerator;
import com.nodestand.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AutoSignUpHandler implements ConnectionSignUp {

    private final UserRepository userRepository;

    private final AliasGenerator aliasGenerator;

    @Autowired
    public AutoSignUpHandler(UserRepository userRepository, AliasGenerator aliasGenerator) {
        this.userRepository = userRepository;
        this.aliasGenerator = aliasGenerator;
    }

    @Override
    @Transactional
    public String execute(final Connection<?> connection) {
        //add new users to the db with its default roles for later use in SocialAuthenticationSuccessHandler
        final User user = new User(
                connection.getKey().getProviderId(),
                connection.getKey().getProviderUserId(),
                User.Roles.ROLE_USER);

        // Start with three aliases
        user.addNewAlias(generateUniqueUserName());
        user.addNewAlias(generateUniqueUserName());
        user.addNewAlias(generateUniqueUserName());

        userRepository.save(user);
        return user.getStableId();
    }


    private String generateUniqueUserName() {

        for (int i = 0; i < 50; i++) {
            String name = aliasGenerator.generateAlias();
            if (userRepository.findByAlias(name) == null) {
                return name;
            }
        }

        // Very bad luck!
        return IdGenerator.newId();
    }
}
