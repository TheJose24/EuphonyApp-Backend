package com.euphony.streaming.exception.custom.subcriptionplans;

import com.euphony.streaming.exception.HttpStatusProvider;
import org.springframework.http.HttpStatus;

public class SubscriptionPlansNotFoundException extends RuntimeException implements HttpStatusProvider {

    private final HttpStatus httpStatus;

    public SubscriptionPlansNotFoundException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public SubscriptionPlansNotFoundException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public SubscriptionPlansNotFoundException(Throwable cause, HttpStatus httpStatus) {
        super(cause);
        this.httpStatus = httpStatus;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
