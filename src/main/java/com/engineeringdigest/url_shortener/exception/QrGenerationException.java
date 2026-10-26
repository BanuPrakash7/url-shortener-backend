package com.engineeringdigest.url_shortener.exception;

public class QrGenerationException extends RuntimeException {
    public QrGenerationException(String message) {
        super(message);
    }
}
