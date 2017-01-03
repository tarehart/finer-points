package com.nodestand.nodes.interpretation;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.Author;
import com.nodestand.nodes.ImmutableNodeException;
import com.nodestand.nodes.version.MajorVersion;
import com.nodestand.util.BodyParser;

import java.util.Set;

public class InterpretationBody extends ArgumentBody {

    String body;

    public InterpretationBody() {}

    @Override
    public InterpretationNode constructNode() {

        return new InterpretationNode(this);
    }

    @Override
    public Set<String> getMajorVersionsFromBodyText() {
        return BodyParser.getMajorVersions(body);
    }

    public InterpretationBody(String title, String qualifier, String body, Author author) {
        this(title, qualifier, body, author, null);
    }

    public InterpretationBody(String title, String qualifier, String body, Author author, MajorVersion majorVersion) {
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
