package com.example.conmon.exception;

public class DuplicateRegistrationException extends RuntimeException {
    public DuplicateRegistrationException() { super("DUPLICATE_REGISTRATION"); }
    public DuplicateRegistrationException(String message) { super(message); }
}

