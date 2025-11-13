package com.example.conmon.exception;

public class SourceFullException extends RuntimeException {
    public SourceFullException() { super("SOURCE_FULL"); }
    public SourceFullException(String message) { super(message); }
}

