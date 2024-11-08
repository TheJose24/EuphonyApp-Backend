package com.euphony.streaming.exception.custom.user;

import com.euphony.streaming.exception.HttpStatusProvider;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserUpdateException extends RuntimeException implements HttpStatusProvider {

    private final HttpStatus httpStatus;

    public UserUpdateException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public UserUpdateException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public UserUpdateException(Throwable cause, HttpStatus httpStatus) {
        super(cause);
        this.httpStatus = httpStatus;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
