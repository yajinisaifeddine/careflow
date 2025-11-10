package com.careflow.exceptions.auth;

public class TooManyResetAttemptsException extends RuntimeException {
    public TooManyResetAttemptsException(String message) {
        super(message);
    }
}
