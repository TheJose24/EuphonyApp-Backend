package com.euphony.streaming.exception.advice;

import com.euphony.streaming.exception.HttpStatusProvider;
import com.euphony.streaming.exception.custom.artist.ArtistCreationException;
import com.euphony.streaming.exception.custom.artist.ArtistDeletionException;
import com.euphony.streaming.exception.custom.artist.ArtistNotFoundException;
import com.euphony.streaming.exception.custom.artist.ArtistUpdateException;
import com.euphony.streaming.exception.custom.follow.FollowAlreadyExistsException;
import com.euphony.streaming.exception.custom.follow.FollowBadRequestException;
import com.euphony.streaming.exception.custom.follow.FollowNotFoundException;
import com.euphony.streaming.exception.custom.genre.GenreCreationException;
import com.euphony.streaming.exception.custom.genre.GenreDeletionException;
import com.euphony.streaming.exception.custom.genre.GenreNotFoundException;
import com.euphony.streaming.exception.custom.genre.GenreUpdateException;
import com.euphony.streaming.exception.custom.playlist.PlaylistCreationException;
import com.euphony.streaming.exception.custom.playlist.PlaylistDeletionException;
import com.euphony.streaming.exception.custom.playlist.PlaylistNotFoundException;
import com.euphony.streaming.exception.custom.playlist.PlaylistUpdateException;
import com.euphony.streaming.exception.custom.user.UserCreationException;
import com.euphony.streaming.exception.custom.user.UserDeletionException;
import com.euphony.streaming.exception.custom.user.UserNotFoundException;
import com.euphony.streaming.exception.custom.user.UserUpdateException;
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
    })
    public ResponseEntity<String> handlePlaylistExceptions(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), getStatusFromException(ex));
    }

    // Manejo de excepciones para Géneros
    @ExceptionHandler({
            GenreCreationException.class,
            GenreNotFoundException.class,
            GenreUpdateException.class,
            GenreDeletionException.class,
    })
    public ResponseEntity<String> handleGenreExceptions(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), getStatusFromException(ex));
    }

    // Manejo de excepciones para Artistas
    @ExceptionHandler({
            ArtistCreationException.class,
            ArtistNotFoundException.class,
            ArtistUpdateException.class,
            ArtistDeletionException.class,
    })
    public ResponseEntity<String> handleArtistExceptions(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), getStatusFromException(ex));
    }

    // Manejo de excepciones para Seguidores
    @ExceptionHandler({
            FollowAlreadyExistsException.class,
            FollowBadRequestException.class,
            FollowNotFoundException.class
    })
    public ResponseEntity<String> handleFollowExceptions(RuntimeException ex) {
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
