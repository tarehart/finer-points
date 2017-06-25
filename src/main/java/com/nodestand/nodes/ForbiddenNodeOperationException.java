package com.nodestand.nodes;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ForbiddenNodeOperationException extends NodeRulesException {
    public ForbiddenNodeOperationException(String message) {
        super(message);
    }
}
