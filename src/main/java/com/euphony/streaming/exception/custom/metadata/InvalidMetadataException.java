package com.euphony.streaming.exception.custom.metadata;

import com.euphony.streaming.exception.HttpStatusProvider;
import org.springframework.http.HttpStatus;

public class InvalidMetadataException extends RuntimeException implements HttpStatusProvider {

    private final HttpStatus httpStatus;

    public InvalidMetadataException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public InvalidMetadataException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public InvalidMetadataException(Throwable cause, HttpStatus httpStatus) {
        super(cause);
        this.httpStatus = httpStatus;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}