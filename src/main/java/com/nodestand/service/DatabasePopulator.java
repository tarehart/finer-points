package com.nodestand.service;

import com.nodestand.nodes.*;
import com.nodestand.nodes.ArgumentNodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author mh
 * @since 04.03.11
 */
@Service
public class DatabasePopulator {

    @Autowired
    UserRepository userRepository;

    @Autowired
    NodeUserDetailsService udService;

    @Autowired
    PasswordEncoderService passwordService;

    @Autowired ArgumentNodeRepository argumentRepository;

    private final static Logger log = LoggerFactory.getLogger(DatabasePopulator.class);

    @Transactional
    public void populateDatabase() {
        User me = userRepository.save(new User("tarehart", "Tyler", "password", passwordService, User.Roles.ROLE_ADMIN, User.Roles.ROLE_USER));
        udService.register("charles", "Charles", "password");
        //User charles = new User("charles", "Charles", "password",User.Roles.ROLE_USER);
        //userRepository.save(me);
        //userRepository.save(charles);

        Assertion tablesHelpful = new Assertion("Tables are helpful for meals.");

        Assertion mealsBenefit = new Assertion("It is easier to eat a meal if you have a flat surface");
        Interpretation tablesInterp = new Interpretation("Tables weekly says that tables provide a flat surface");

        Source tablesWeekly = new Source("Tables Weekly, vol 32", "http://www.google.com");


        argumentRepository.save(tablesHelpful);
        argumentRepository.save(mealsBenefit);
        argumentRepository.save(tablesInterp);
        argumentRepository.save(tablesWeekly);

        tablesHelpful.supportedBy(tablesInterp);
        tablesHelpful.supportedBy(mealsBenefit);

        tablesInterp.setSource(tablesWeekly);

        argumentRepository.save(tablesHelpful);
        argumentRepository.save(tablesInterp);

    }

}
