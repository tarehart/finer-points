package com.nodestand.nodes.assertion;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.ImmutableNodeException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.version.MajorVersion;
import org.neo4j.ogm.annotation.NodeEntity;


@NodeEntity
public class AssertionBody extends ArgumentBody {

    String body;

    public AssertionBody() {}

    @Override
    public AssertionNode constructNode() {
        return new AssertionNode(this);
    }

    public AssertionBody(String title, String qualifier, String body, User author) {
        this(title, qualifier, body, author, null);
    }

    public AssertionBody(String title, String qualifier, String body, User author, MajorVersion majorVersion) {
        super(title, qualifier, author, majorVersion);

        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) throws ImmutableNodeException {
        this.body = body;
    }
}
