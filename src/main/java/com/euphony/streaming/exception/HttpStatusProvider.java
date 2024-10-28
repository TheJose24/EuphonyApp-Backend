package com.euphony.streaming.exception;

import org.springframework.http.HttpStatus;

public interface HttpStatusProvider {
    HttpStatus getHttpStatus();
}
