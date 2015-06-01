package com.nodestand.nodes.interpretation;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.User;
import com.nodestand.nodes.version.Build;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class InterpretationBody extends ArgumentBody {

    String body;

    public InterpretationBody() {}

    @Override
    public ArgumentNode constructNode(Build build) {
        return new InterpretationNode(this, build);
    }

    public InterpretationBody(String title, String body, User author) {
        super(title, author);

        this.body = body;
    }

}
