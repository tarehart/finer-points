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

    @Autowired CommentRepository commentRepository;

    private final static Logger log = LoggerFactory.getLogger(DatabasePopulator.class);

    @Transactional
    public void populateDatabase() {
        User me = userRepository.save(new User("tarehart", "Tyler", "pw", passwordService, User.Roles.ROLE_ADMIN, User.Roles.ROLE_USER));
        User charles = udService.register("charles", "Charles", "pw"); // register automatically makes it a non-admin

        //User charles = new User("charles", "Charles", "password",User.Roles.ROLE_USER);
        //userRepository.save(me);
        //userRepository.save(charles);

        Assertion tablesHelpful = new Assertion("Tables are helpful for meals.", me);

        Assertion mealsBenefit = new Assertion("It is easier to eat a meal if you have a flat surface", me);
        Interpretation tablesInterp = new Interpretation("Tables weekly says that tables provide a flat surface", charles);

        Source tablesWeekly = new Source("Tables Weekly, vol 32", charles, "http://www.google.com");


        argumentRepository.save(tablesHelpful);
        argumentRepository.save(mealsBenefit);
        argumentRepository.save(tablesInterp);
        argumentRepository.save(tablesWeekly);

        tablesHelpful.supportedBy(tablesInterp);
        tablesHelpful.supportedBy(mealsBenefit);

        tablesInterp.setSource(tablesWeekly);

        argumentRepository.save(tablesHelpful);
        argumentRepository.save(tablesInterp);

        Comment c1 = new Comment(tablesHelpful, charles, "I agree in principle, but can you refine the logic?");
        c1.registerUpVote(me);
        commentRepository.save(c1);

        Comment c2 = new Comment(c1, me, "Can you be more specific?");
        commentRepository.save(c2);

    }

}
