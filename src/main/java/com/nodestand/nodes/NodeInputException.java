package com.nodestand.nodes;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class NodeInputException extends NodeRulesException {
    public NodeInputException(String message) {
        super(message);
    }
}
