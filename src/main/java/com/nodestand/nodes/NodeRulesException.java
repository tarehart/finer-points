package com.nodestand.nodes;

/**
 * This exception means that we are attempting to arrange nodes in a way that breaks the constraints of the system.
 */
public class NodeRulesException extends Exception {

    public NodeRulesException(String message) {
        super(message);
    }
}
