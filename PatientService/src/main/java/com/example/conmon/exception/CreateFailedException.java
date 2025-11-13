package com.example.conmon.exception;

public class CreateFailedException extends RuntimeException {
    public CreateFailedException() { super("CREATE_FAILED"); }
    public CreateFailedException(String message) { super(message); }
}

