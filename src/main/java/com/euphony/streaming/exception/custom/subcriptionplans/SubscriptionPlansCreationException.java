package com.euphony.streaming.exception.custom.subcriptionplans;

import com.euphony.streaming.exception.HttpStatusProvider;
import org.springframework.http.HttpStatus;

public class SubscriptionPlansCreationException extends RuntimeException implements HttpStatusProvider {

    private final HttpStatus httpStatus;

    public SubscriptionPlansCreationException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public SubscriptionPlansCreationException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public SubscriptionPlansCreationException(Throwable cause, HttpStatus httpStatus) {
        super(cause);
        this.httpStatus = httpStatus;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
