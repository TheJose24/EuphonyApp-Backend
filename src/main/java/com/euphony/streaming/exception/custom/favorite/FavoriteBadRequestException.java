package com.euphony.streaming.exception.custom.favorite;

import com.euphony.streaming.exception.HttpStatusProvider;
import org.springframework.http.HttpStatus;

/**
 * Excepción para solicitudes inválidas del módulo de favoritos (request nulo, IDs nulos, etc.).
 */
public class FavoriteBadRequestException extends RuntimeException implements HttpStatusProvider {

    private final HttpStatus httpStatus;

    public FavoriteBadRequestException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public FavoriteBadRequestException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public FavoriteBadRequestException(Throwable cause, HttpStatus httpStatus) {
        super(cause);
        this.httpStatus = httpStatus;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
