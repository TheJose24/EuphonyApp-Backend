package com.euphony.streaming.exception.custom.metadata;

import com.euphony.streaming.exception.HttpStatusProvider;
import org.springframework.http.HttpStatus;

public class MetadataProcessingException extends RuntimeException implements HttpStatusProvider {

    private final HttpStatus httpStatus;

    public MetadataProcessingException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public MetadataProcessingException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public MetadataProcessingException(Throwable cause, HttpStatus httpStatus) {
        super(cause);
        this.httpStatus = httpStatus;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
