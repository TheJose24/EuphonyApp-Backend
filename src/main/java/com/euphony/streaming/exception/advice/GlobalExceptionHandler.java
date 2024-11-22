package com.euphony.streaming.exception.advice;

import com.euphony.streaming.exception.HttpStatusProvider;
import com.euphony.streaming.exception.custom.album.AlbumCreationException;
import com.euphony.streaming.exception.custom.album.AlbumDeletionException;
import com.euphony.streaming.exception.custom.album.AlbumNotFoundException;
import com.euphony.streaming.exception.custom.album.AlbumUpdateException;
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
import com.euphony.streaming.exception.custom.metadata.InvalidMetadataException;
import com.euphony.streaming.exception.custom.metadata.MetadataProcessingException;
import com.euphony.streaming.exception.custom.playlist.PlaylistCreationException;
import com.euphony.streaming.exception.custom.playlist.PlaylistDeletionException;
import com.euphony.streaming.exception.custom.playlist.PlaylistNotFoundException;
import com.euphony.streaming.exception.custom.playlist.PlaylistUpdateException;
import com.euphony.streaming.exception.custom.song.SongCreationException;
import com.euphony.streaming.exception.custom.song.SongDeletionException;
import com.euphony.streaming.exception.custom.song.SongNotFoundException;
import com.euphony.streaming.exception.custom.song.SongUpdateException;
import com.euphony.streaming.exception.custom.storage.FileStorageException;
import com.euphony.streaming.exception.custom.subcriptionplans.SubscriptionPlansCreationException;
import com.euphony.streaming.exception.custom.subcriptionplans.SubscriptionPlansDeletionException;
import com.euphony.streaming.exception.custom.subcriptionplans.SubscriptionPlansNotFoundException;
import com.euphony.streaming.exception.custom.subcriptionplans.SubscriptionPlansUpdateException;
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

    // Manejo de excepciones para Archivos
    @ExceptionHandler({
            FileStorageException.class
    })
    public ResponseEntity<String> handleFileStorageExceptions(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), getStatusFromException(ex));
    }

    // Manejo de excepciones para Álbumes
    @ExceptionHandler({
            AlbumCreationException.class,
            AlbumNotFoundException.class,
            AlbumUpdateException.class,
            AlbumDeletionException.class,
    })
    public ResponseEntity<String> handleAlbumExceptions(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), getStatusFromException(ex));
    }

    // Manejo de excepciones para Metadatos de canciones
    @ExceptionHandler({
            InvalidMetadataException.class,
            MetadataProcessingException.class,
    })
    public ResponseEntity<String> handleMetadataExceptions(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), getStatusFromException(ex));
    }

    // Manejo de excepciones para Canciones
    @ExceptionHandler({
            SongCreationException.class,
            SongNotFoundException.class,
            SongUpdateException.class,
            SongDeletionException.class,
    })
    public ResponseEntity<String> handleSongExceptions(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), getStatusFromException(ex));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(Exception ex) {
        return new ResponseEntity<>("Error interno del servidor. Por favor, intente más tarde.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    // Manejo de excepciones para Suscripciones
    @ExceptionHandler({
            SubscriptionPlansCreationException.class,
            SubscriptionPlansNotFoundException.class,
            SubscriptionPlansUpdateException.class,
            SubscriptionPlansDeletionException.class,
    })
    public ResponseEntity<String> handleSubscriptionExceptions(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), getStatusFromException(ex));
    }

    private HttpStatus getStatusFromException(RuntimeException ex) {
        if (ex instanceof HttpStatusProvider) {
            return ((HttpStatusProvider) ex).getHttpStatus();
        }
        return HttpStatus.BAD_REQUEST;  // Default status if none provided
    }
}
