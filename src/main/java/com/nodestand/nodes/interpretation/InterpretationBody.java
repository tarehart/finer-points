package com.nodestand.nodes.interpretation;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.ImmutableNodeException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.version.MajorVersion;
import com.nodestand.nodes.version.VersionHelper;

public class InterpretationBody extends ArgumentBody {

    String body;

    public InterpretationBody() {}

    @Override
    public InterpretationNode constructNode(VersionHelper versionHelper) {

        return new InterpretationNode(this, VersionHelper.startBuild(author));
    }

    public InterpretationBody(String title, String body, User author) {
        this(title, body, author, null);
    }

    public InterpretationBody(String title, String body, User author, MajorVersion majorVersion) {
        super(title, author, majorVersion);

        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) throws ImmutableNodeException {
        if (!isDraft()) {
            throw new ImmutableNodeException("Cannot edit a title unless the argument is in draft mode. Must create a new version.");
        }
        this.body = body;
    }

}
