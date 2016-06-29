package com.nodestand.nodes.version;

import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.comment.Commentable;
import com.nodestand.nodes.vote.ArgumentVote;
import com.nodestand.nodes.vote.VoteType;
import com.nodestand.util.IdGenerator;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

@NodeEntity
public class MajorVersion implements Commentable {

    @GraphId
    protected Long id;

    private String stableId;

    private int versionNumber;

    @Relationship(type="VERSION_OF", direction = Relationship.OUTGOING)
    protected VersionAggregator versionAggregator;

    @Relationship(type="ARGUMENT_VOTE", direction = Relationship.INCOMING)
    private Set<ArgumentVote> argumentVotes;

    // I'm not using a map for this because you're not allowed to save maps on nodes.
    public int greatVotes;
    public int weakVotes;
    public int toucheVotes;
    public int trashVotes;

    public MajorVersion() {}

    public MajorVersion(int versionNumber, VersionAggregator versionAggregator) {
        this.versionNumber = versionNumber;
        this.versionAggregator = versionAggregator;
        this.stableId = IdGenerator.newId();
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getStableId() {
        return stableId;
    }

    public void decrementVote(VoteType voteType) throws NodeRulesException {
        switch (voteType) {
            case GREAT:
                greatVotes--;
                break;
            case WEAK:
                weakVotes--;
                break;
            case TOUCHE:
                toucheVotes--;
                break;
            case TRASH:
                trashVotes--;
                break;
            default:
                throw new NodeRulesException("Unexpected vote type: " + voteType.name());
        }
    }

    public void incrementVote(VoteType voteType) throws NodeRulesException {
        switch (voteType) {
            case GREAT:
                greatVotes++;
                break;
            case WEAK:
                weakVotes++;
                break;
            case TOUCHE:
                toucheVotes++;
                break;
            case TRASH:
                trashVotes++;
                break;
            default:
                throw new NodeRulesException("Unexpected vote type: " + voteType.name());
        }
    }
}
