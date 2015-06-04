package com.nodestand.nodes.assertion;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.User;
import com.nodestand.nodes.version.Build;
import com.nodestand.nodes.version.VersionHelper;
import org.springframework.data.neo4j.annotation.NodeEntity;


@NodeEntity
public class AssertionBody extends ArgumentBody {

    String body;

    public AssertionBody() {}

    @Override
    public AssertionNode constructNode(VersionHelper versionHelper) {
        return new AssertionNode(this, versionHelper.beginBodyBuild(this));
    }

    public AssertionBody(String title, String body, User author) {
        super(title, author);

        this.body = body;
    }
}
