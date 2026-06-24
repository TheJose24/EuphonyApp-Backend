package com.euphony.streaming.exception.custom.report;

import com.euphony.streaming.exception.HttpStatusProvider;
import org.springframework.http.HttpStatus;

public class ReportGenerationSaveException extends RuntimeException implements HttpStatusProvider {

    private final HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

    public ReportGenerationSaveException(String message) {
        super(message);
    }

    public ReportGenerationSaveException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}