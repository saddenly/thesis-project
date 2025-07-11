package com.rustem.eduthesis.infrastructure.exception;

public class NotStudentException extends RuntimeException {
    public NotStudentException(String message) {
        super(message);
    }
}
