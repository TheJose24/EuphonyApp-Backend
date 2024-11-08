package com.euphony.streaming.exception.custom.genre;

import com.euphony.streaming.exception.HttpStatusProvider;
import org.springframework.http.HttpStatus;

public class GenreNotFoundException extends RuntimeException implements HttpStatusProvider {

    private final HttpStatus httpStatus;

    public GenreNotFoundException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public GenreNotFoundException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public GenreNotFoundException(Throwable cause, HttpStatus httpStatus) {
        super(cause);
        this.httpStatus = httpStatus;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}