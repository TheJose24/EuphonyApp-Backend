package com.euphony.streaming.exception.custom;

import com.euphony.streaming.exception.HttpStatusProvider;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserDeletionException extends RuntimeException implements HttpStatusProvider {

    private final HttpStatus httpStatus;

    public UserDeletionException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public UserDeletionException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public UserDeletionException(Throwable cause, HttpStatus httpStatus) {
        super(cause);
        this.httpStatus = httpStatus;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
