package com.launchwindow.exception;

public class InvalidPaginationException extends RuntimeException {
    public InvalidPaginationException(String message) {
        super(message);
    }
}
