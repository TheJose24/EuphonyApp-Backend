package com.euphony.streaming.exception.custom.user;

import com.euphony.streaming.exception.HttpStatusProvider;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends RuntimeException implements HttpStatusProvider {

    private final HttpStatus httpStatus;

    public UserNotFoundException(HttpStatus httpStatus) {
        super();
        this.httpStatus = httpStatus;
    }

    public UserNotFoundException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public UserNotFoundException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public UserNotFoundException(Throwable cause, HttpStatus httpStatus) {
        super(cause);
        this.httpStatus = httpStatus;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
