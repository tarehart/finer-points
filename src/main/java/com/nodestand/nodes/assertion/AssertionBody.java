package com.nodestand.nodes.assertion;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.User;
import com.nodestand.nodes.version.MajorVersion;
import com.nodestand.nodes.version.VersionHelper;
import org.springframework.data.neo4j.annotation.NodeEntity;


@NodeEntity
public class AssertionBody extends ArgumentBody {

    String body;

    public AssertionBody() {}

    @Override
    public AssertionNode constructNode(VersionHelper versionHelper) {
        return new AssertionNode(this, versionHelper.startBuild(this));
    }

    public AssertionBody(String title, String body, User author) {
        this(title, body, author, null);
    }

    public AssertionBody(String title, String body, User author, MajorVersion majorVersion) {
        super(title, author, majorVersion);

        this.body = body;
    }
}
