package com.euphony.streaming.controller;

import com.euphony.streaming.dto.request.FavoriteAlbumRequestDTO;
import com.euphony.streaming.dto.request.FavoriteSongRequestDTO;
import com.euphony.streaming.dto.response.AlbumResponseDTO;
import com.euphony.streaming.dto.response.SongResponseDTO;
import com.euphony.streaming.service.interfaces.IFavoritesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/favorites")
@Tag(name = "Gestión de Favoritos", description = "API para gestionar canciones con 'me gusta' y álbumes favoritos por usuario")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__(@Lazy))
@Validated
public class FavoritesController {

    private final IFavoritesService favoritesService;

    // ----------------------------- Canciones (likes) -----------------------------

    @Operation(summary = "Dar 'me gusta' a una canción",
            description = "Marca una canción como favorita del usuario. Idempotente: dar like dos veces no produce error.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Canción marcada como favorita", content = @Content),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario o canción no encontrados", content = @Content)
    })
    @PostMapping("/songs")
    public ResponseEntity<Void> likeSong(@RequestBody @Validated FavoriteSongRequestDTO request) {
        log.debug("REST request para dar like a canción: {}", request);
        favoritesService.likeSong(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Quitar 'me gusta' de una canción",
            description = "Quita una canción de los favoritos del usuario. Idempotente: quitar algo que no estaba no produce error.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Canción quitada de favoritas", content = @Content),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content)
    })
    @DeleteMapping("/songs")
    public ResponseEntity<Void> unlikeSong(@RequestBody @Validated FavoriteSongRequestDTO request) {
        log.debug("REST request para quitar like a canción: {}", request);
        favoritesService.unlikeSong(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Canciones favoritas de un usuario",
            description = "Devuelve las canciones marcadas con 'me gusta' por el usuario, con artista, álbum y géneros resueltos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de canciones favoritas",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SongResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    @GetMapping("/songs/by-user/{userId}")
    public ResponseEntity<List<SongResponseDTO>> getFavoriteSongs(
            @Parameter(description = "ID del usuario (UUID)", required = true)
            @PathVariable UUID userId) {
        log.debug("REST request para obtener canciones favoritas del usuario: {}", userId);
        return ResponseEntity.ok(favoritesService.getFavoriteSongs(userId));
    }

    // ----------------------------- Álbumes (favoritos) -----------------------------

    @Operation(summary = "Marcar un álbum como favorito",
            description = "Marca un álbum como favorito del usuario. Idempotente: marcar dos veces no produce error.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Álbum marcado como favorito", content = @Content),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario o álbum no encontrados", content = @Content)
    })
    @PostMapping("/albums")
    public ResponseEntity<Void> favoriteAlbum(@RequestBody @Validated FavoriteAlbumRequestDTO request) {
        log.debug("REST request para marcar álbum como favorito: {}", request);
        favoritesService.favoriteAlbum(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Quitar un álbum de favoritos",
            description = "Quita un álbum de los favoritos del usuario. Idempotente: quitar algo que no estaba no produce error.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Álbum quitado de favoritos", content = @Content),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content)
    })
    @DeleteMapping("/albums")
    public ResponseEntity<Void> unfavoriteAlbum(@RequestBody @Validated FavoriteAlbumRequestDTO request) {
        log.debug("REST request para quitar álbum de favoritos: {}", request);
        favoritesService.unfavoriteAlbum(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Álbumes favoritos de un usuario",
            description = "Devuelve los álbumes marcados como favoritos por el usuario (con su artista anidado).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de álbumes favoritos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AlbumResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    @GetMapping("/albums/by-user/{userId}")
    public ResponseEntity<List<AlbumResponseDTO>> getFavoriteAlbums(
            @Parameter(description = "ID del usuario (UUID)", required = true)
            @PathVariable UUID userId) {
        log.debug("REST request para obtener álbumes favoritos del usuario: {}", userId);
        return ResponseEntity.ok(favoritesService.getFavoriteAlbums(userId));
    }
}
