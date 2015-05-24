package com.nodestand.nodes.interpretation;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.User;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class InterpretationBody extends ArgumentBody {

    String body;

    public InterpretationBody() {}

    public InterpretationBody(String title, String body, User author) {
        super(title, author);

        this.body = body;
    }

}
