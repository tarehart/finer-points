package com.nodestand.nodes;

/**
 * This exception means that some code has incorrectly tried to edit an existing node rather than make a fresh copy.
 * Nodes are always immutable. The VersionHelper can assist with creating fresh copies to hold edits.
 */
public class ImmutableNodeException extends Exception {

    public ImmutableNodeException(String message) {
        super(message);
    }
}
