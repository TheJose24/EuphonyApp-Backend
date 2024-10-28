package com.euphony.streaming.exception.advice;

import com.euphony.streaming.exception.HttpStatusProvider;
import com.euphony.streaming.exception.custom.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Manejo de excepciones para Usuarios
    @ExceptionHandler({
            UserCreationException.class,
            UserNotFoundException.class,
            UserUpdateException.class,
            UserDeletionException.class
    })
    public ResponseEntity<String> handleUserExceptions(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), getStatusFromException(ex));
    }

    // Manejo de excepciones para Playlists
    @ExceptionHandler({
            PlaylistCreationException.class,
            PlaylistNotFoundException.class,
            PlaylistUpdateException.class,
            PlaylistDeletionException.class,
            PlaylistSongException.class
    })
    public ResponseEntity<String> handlePlaylistExceptions(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), getStatusFromException(ex));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(Exception ex) {
        return new ResponseEntity<>("Error interno del servidor. Por favor, intente más tarde.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private HttpStatus getStatusFromException(RuntimeException ex) {
        if (ex instanceof HttpStatusProvider) {
            return ((HttpStatusProvider) ex).getHttpStatus();
        }
        return HttpStatus.BAD_REQUEST;  // Default status if none provided
    }
}
