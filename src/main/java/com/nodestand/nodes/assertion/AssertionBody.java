package com.nodestand.nodes.assertion;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.User;
import com.nodestand.nodes.version.Build;
import org.springframework.data.neo4j.annotation.NodeEntity;


@NodeEntity
public class AssertionBody extends ArgumentBody {

    String body;

    public AssertionBody() {}

    @Override
    public ArgumentNode constructNode(Build build) {
        return new AssertionNode(this, build);
    }

    public AssertionBody(String title, String body, User author) {
        super(title, author);

        this.body = body;
    }
}
