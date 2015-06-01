package com.nodestand.service;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.ArgumentNodeRepository;
import com.nodestand.nodes.User;
import com.nodestand.nodes.UserRepository;
import com.nodestand.nodes.assertion.AssertionBody;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.comment.Comment;
import com.nodestand.nodes.comment.CommentRepository;
import com.nodestand.nodes.interpretation.InterpretationBody;
import com.nodestand.nodes.interpretation.InterpretationNode;
import com.nodestand.nodes.source.SourceBody;
import com.nodestand.nodes.source.SourceNode;
import com.nodestand.nodes.version.Build;
import com.nodestand.nodes.version.VersionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabasePopulator {

    @Autowired
    UserRepository userRepository;

    @Autowired
    NodeUserDetailsService udService;

    @Autowired
    ArgumentNodeRepository argumentRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    VersionHelper versionHelper;

    private final static Logger log = LoggerFactory.getLogger(DatabasePopulator.class);

    @Transactional
    public void populateDatabase() {
        User me = userRepository.save(new User("116023433898470559862", "Tyler", User.Roles.ROLE_ADMIN, User.Roles.ROLE_USER));
        User charles = udService.register("charles-social-id", "Charles").getUser(); // register automatically makes it a non-admin

        Build build = new Build();
        build.author = me;

        AssertionBody mealsBenefitBody = new AssertionBody("It is easier to eat a meal if you have a flat surface",
                "Meals are easier to eat if you have a flat surface because your sandwich won't roll around.", me);

        ArgumentNode mealsBenefitNode = versionHelper.constructVersionedNode(mealsBenefitBody);

        InterpretationBody tablesInterpBody = new InterpretationBody("Tables provide a flat surface",
                "Tables Weekly suggests that tables provide a flat surface based on my reading of the third paragraph.", charles);

        InterpretationNode tablesInterpNode = (InterpretationNode) versionHelper.constructVersionedNode(tablesInterpBody);

        SourceBody tablesWeeklyBody = new SourceBody("Tables Weekly, vol 32", charles, "http://www.google.com");

        SourceNode tablesWeeklyNode = (SourceNode) versionHelper.constructVersionedNode(tablesWeeklyBody);

        argumentRepository.save(mealsBenefitNode);
        argumentRepository.save(tablesInterpNode);
        argumentRepository.save(tablesWeeklyNode);

        AssertionBody tablesHelpfulBody = new AssertionBody("Tables are helpful for meals.",
                "Tables help with meals because {{[" + tablesInterpNode.getId() +
                        "]They provide a flat surface}} which is {{[" + mealsBenefitNode.getId() + "]helpful}}.", me);

        AssertionNode tablesHelpfulNode = (AssertionNode) versionHelper.constructVersionedNode(tablesHelpfulBody);

        argumentRepository.save(tablesHelpfulNode);

        tablesHelpfulNode.supportedBy(tablesInterpNode);
        tablesHelpfulNode.supportedBy(mealsBenefitNode);

        tablesInterpNode.setSource(tablesWeeklyNode);

        argumentRepository.save(tablesHelpfulNode);
        argumentRepository.save(tablesInterpNode);

        Comment c1 = new Comment(tablesHelpfulBody, charles, "I agree in principle, but can you refine the logic?");
        c1.registerUpVote(me);
        commentRepository.save(c1);

        Comment c2 = new Comment(c1, me, "Can you be more specific?");
        commentRepository.save(c2);

    }

}
