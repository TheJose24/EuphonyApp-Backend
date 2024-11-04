package com.euphony.streaming.exception.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PlaylistUpdateException extends RuntimeException {

    private final HttpStatus httpStatus;

    public PlaylistUpdateException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public PlaylistUpdateException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public PlaylistUpdateException(Throwable cause, HttpStatus httpStatus) {
        super(cause);
        this.httpStatus = httpStatus;
    }
}