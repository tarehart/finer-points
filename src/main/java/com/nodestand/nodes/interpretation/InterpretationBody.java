package com.nodestand.nodes.interpretation;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.User;
import com.nodestand.nodes.version.Build;
import com.nodestand.nodes.version.VersionHelper;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class InterpretationBody extends ArgumentBody {

    String body;

    public InterpretationBody() {}

    @Override
    public InterpretationNode constructNode(VersionHelper versionHelper) {
        return new InterpretationNode(this, versionHelper.beginBodyBuild(this));
    }

    public InterpretationBody(String title, String body, User author) {
        super(title, author);

        this.body = body;
    }

}
