package com.nodestand.nodes;

import com.nodestand.nodes.comment.Commentable;
import com.nodestand.nodes.version.Build;
import com.nodestand.nodes.version.MajorVersion;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

@NodeEntity
public abstract class ArgumentBody implements Commentable {

    @GraphId
    protected Long id;

    private String title;

    @RelatedTo(type="AUTHORED_BY", direction = Direction.OUTGOING)
    public User author;

    @RelatedTo(type="VERSION_OF", direction = Direction.OUTGOING)
    private MajorVersion majorVersion;
    private int minorVersion;

    public ArgumentBody() {}

    public ArgumentBody(String title, User author) {
        this.title = title;
        this.author = author;
    }

    public void setVersion(MajorVersion majorVersion, int minorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    public String getTitle() {
        return title;
    }

    public MajorVersion getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public abstract ArgumentNode constructNode(Build build);

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }
}
