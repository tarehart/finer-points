package com.nodestand.auth;

public class NotAuthorizedException extends Exception {

    public NotAuthorizedException(String message) {
        super(message);
    }
}
