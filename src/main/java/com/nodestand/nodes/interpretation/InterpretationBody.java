package com.nodestand.nodes.interpretation;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.User;
import com.nodestand.nodes.source.SourceNode;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

@NodeEntity
public class InterpretationBody extends ArgumentBody {

    String body;

    public InterpretationBody() {}

    public InterpretationBody(String title, String body, User author) {
        super(title, author);

        this.body = body;
    }

}
