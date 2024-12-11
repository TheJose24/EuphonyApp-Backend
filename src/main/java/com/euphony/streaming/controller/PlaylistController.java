package com.euphony.streaming.controller;

import com.euphony.streaming.dto.request.PlaylistRequestDTO;
import com.euphony.streaming.dto.response.PlaylistResponseDTO;
import com.euphony.streaming.dto.response.SongInPlaylistResponseDTO;
import com.euphony.streaming.service.implementation.PlaylistServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/playlists")
@Tag(name = "Gestión de Playlists", description = "API para la gestión de listas de reproducción")
@Slf4j
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistServiceImpl playlistService;

    @Operation(summary = "Obtener todas las playlists",
            description = "Recupera una lista de todas las listas de reproducción disponibles")
    @ApiResponse(responseCode = "200", description = "Playlists recuperadas exitosamente",
            content = @Content(schema = @Schema(implementation = PlaylistResponseDTO.class)))
    @GetMapping("/all")
    public ResponseEntity<List<PlaylistResponseDTO>> getAllPlaylists() {
        log.info("Solicitando todas las playlists");
        List<PlaylistResponseDTO> playlists = playlistService.findAllPlaylists();
        log.info("Se encontraron {} playlists", playlists.size());
        return ResponseEntity.ok(playlists);
    }

    @Operation(summary = "Obtener playlist por ID",
            description = "Recupera una lista de reproducción específica por su ID")
    @ApiResponse(responseCode = "200", description = "Playlist encontrada",
            content = @Content(schema = @Schema(implementation = PlaylistResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Playlist no encontrada")
    @GetMapping("/search/{id}")
    public ResponseEntity<PlaylistResponseDTO> getPlaylistById(
            @Parameter(description = "ID de la playlist", required = true)
            @PathVariable Long id) {
        log.info("Buscando playlist con ID: {}", id);
        PlaylistResponseDTO playlist = playlistService.findPlaylistById(id);
        log.info("Playlist encontrada con ID: {}", id);
        return ResponseEntity.ok(playlist);
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Obtener playlists de un usuario",
            description = "Recupera todas las listas de reproducción pertenecientes a un usuario específico"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Playlists recuperadas exitosamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PlaylistResponseDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public ResponseEntity<List<PlaylistResponseDTO>> getPlaylistsByUserId(
            @Parameter(
                    description = "ID del usuario",
                    required = true,
                    schema = @Schema(type = "string", format = "uuid")
            )
            @PathVariable UUID userId) {

        log.info("Buscando playlists del usuario ID: {}", userId);
        List<PlaylistResponseDTO> playlists = playlistService.findPlaylistsByUserId(userId);
        log.info("Se encontraron {} playlists para el usuario {}", playlists.size(), userId);
        return ResponseEntity.ok(playlists);
    }


    @Operation(summary = "Crear nueva playlist",
            description = "Crea una nueva lista de reproducción en el sistema")
    @ApiResponse(responseCode = "201", description = "Playlist creada exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    @PostMapping("/create")
    public ResponseEntity<Void> createPlaylist(
            @Parameter(description = "Datos de la playlist a crear", required = true)
            @Valid @RequestBody PlaylistRequestDTO playlistRequestDTO) {
        log.info("Creando nueva playlist para usuario ID: {}", playlistRequestDTO.getUserId());
        playlistService.createPlaylist(playlistRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Actualizar playlist",
            description = "Actualiza los datos de una lista de reproducción existente")
    @ApiResponse(responseCode = "204", description = "Playlist actualizada exitosamente")
    @ApiResponse(responseCode = "404", description = "Playlist no encontrada")
    @PutMapping("/update/{id}")
    public ResponseEntity<Void> updatePlaylist(
            @Parameter(description = "ID de la playlist a actualizar", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevos datos de la playlist", required = true)
            @Valid @RequestBody PlaylistRequestDTO playlistRequestDTO) {
        log.info("Actualizando playlist ID: {}", id);
        playlistService.updatePlaylist(id, playlistRequestDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Eliminar playlist",
            description = "Elimina una lista de reproducción existente del sistema")
    @ApiResponse(responseCode = "204", description = "Playlist eliminada exitosamente")
    @ApiResponse(responseCode = "404", description = "Playlist no encontrada")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletePlaylist(
            @Parameter(description = "ID de la playlist a eliminar", required = true)
            @PathVariable Long id) {
        log.info("Eliminando playlist ID: {}", id);
        playlistService.deletePlaylist(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{playlistId}/songs")
    @Operation(
            summary = "Obtener canciones de una playlist",
            description = "Recupera todas las canciones que pertenecen a una playlist específica"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Canciones recuperadas exitosamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = SongInPlaylistResponseDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Playlist no encontrada"
            )
    })
    public ResponseEntity<List<SongInPlaylistResponseDTO>> getPlaylistSongs(
            @Parameter(description = "ID de la playlist", required = true)
            @PathVariable Long playlistId) {

        log.info("Obteniendo canciones de la playlist ID: {}", playlistId);
        List<SongInPlaylistResponseDTO> songs = playlistService.getPlaylistSongs(playlistId);
        return ResponseEntity.ok(songs);
    }


    @PostMapping("/{playlistId}/add/songs/{songId}")
    @Operation(
            summary = "Agregar canción a playlist",
            description = "Agrega una canción existente a una playlist específica"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Canción agregada exitosamente a la playlist"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Playlist o canción no encontrada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error en la solicitud"
            )
    })
    public ResponseEntity<Void> addSongToPlaylist(
            @Parameter(description = "ID de la playlist", required = true)
            @PathVariable Long playlistId,
            @Parameter(description = "ID de la canción", required = true)
            @PathVariable Long songId) {

        log.info("Agregando canción ID: {} a playlist ID: {}", songId, playlistId);
        playlistService.addSongToPlaylist(playlistId, songId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{playlistId}/delete/songs/{songId}")
    @Operation(
            summary = "Eliminar canción de playlist",
            description = "Elimina una canción específica de una playlist"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Canción eliminada exitosamente de la playlist"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Playlist o canción no encontrada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error en la solicitud"
            )
    })
    public ResponseEntity<Void> removeSongFromPlaylist(
            @Parameter(description = "ID de la playlist", required = true)
            @PathVariable Long playlistId,
            @Parameter(description = "ID de la canción", required = true)
            @PathVariable Long songId) {

        log.info("Eliminando canción ID: {} de playlist ID: {}", songId, playlistId);
        playlistService.removeSongFromPlaylist(playlistId, songId);
        return ResponseEntity.noContent().build();
    }


}
