package com.nodestand.nodes.assertion;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.Author;
import com.nodestand.nodes.ImmutableNodeException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.version.MajorVersion;
import com.nodestand.util.BodyParser;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.Set;


@NodeEntity
public class AssertionBody extends ArgumentBody {

    private String body;

    public AssertionBody() {}

    @Override
    public AssertionNode constructNode() {
        return new AssertionNode(this);
    }

    @Override
    public Set<String> getMajorVersionsFromBodyText() {
        return BodyParser.getMajorVersions(body);
    }

    public AssertionBody(String title, String qualifier, String body, Author author) {
        this(title, qualifier, body, author, null);
    }

    public AssertionBody(String title, String qualifier, String body, Author author, MajorVersion majorVersion) {
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
