package com.euphony.streaming.exception.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PlaylistDeletionException extends RuntimeException {

    private final HttpStatus httpStatus;

    public PlaylistDeletionException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public PlaylistDeletionException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public PlaylistDeletionException(Throwable cause, HttpStatus httpStatus) {
        super(cause);
        this.httpStatus = httpStatus;
    }
}