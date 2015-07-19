package com.nodestand.nodes.interpretation;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.User;
import com.nodestand.nodes.version.MajorVersion;
import com.nodestand.nodes.version.VersionHelper;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class InterpretationBody extends ArgumentBody {

    String body;

    public InterpretationBody() {}

    @Override
    public InterpretationNode constructNode(VersionHelper versionHelper) {

        return new InterpretationNode(this, versionHelper.startBuild(this));
    }

    public InterpretationBody(String title, String body, User author) {
        this(title, body, author, null);
    }

    public InterpretationBody(String title, String body, User author, MajorVersion majorVersion) {
        super(title, author, majorVersion);

        this.body = body;
    }

}
