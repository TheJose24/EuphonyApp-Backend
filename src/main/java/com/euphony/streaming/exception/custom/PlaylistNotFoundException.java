package com.euphony.streaming.exception.custom;

import org.springframework.http.HttpStatus;

public class PlaylistNotFoundException extends RuntimeException {

    public PlaylistNotFoundException(String message, HttpStatus notFound) {
        super();
    }

    public PlaylistNotFoundException(String message) {
        super(message);
    }

    public PlaylistNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlaylistNotFoundException(Throwable cause) {
        super(cause);
    }
}