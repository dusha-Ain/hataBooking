package com.rent.hatabooking.web.exception;

public class BadCredentialsPasswordException extends RuntimeException {
    public BadCredentialsPasswordException(String message) {
        super(message);
    }
}
