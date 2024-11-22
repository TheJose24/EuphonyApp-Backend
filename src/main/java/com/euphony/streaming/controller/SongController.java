package com.euphony.streaming.controller;

import com.euphony.streaming.dto.request.SongRequestDTO;
import com.euphony.streaming.dto.response.SongMetadataResponseDTO;
import com.euphony.streaming.dto.response.SongResponseDTO;
import com.euphony.streaming.service.interfaces.ISongService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/songs")
@RequiredArgsConstructor
@Tag(name = "Controlador de Canciones", description = "Operaciones para gestionar canciones en la plataforma de streaming")
public class SongController {

    private final ISongService songService;

    @GetMapping("/all")
    @Operation(summary = "Obtener todas las canciones", description = "Recupera una lista de todas las canciones disponibles en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de canciones recuperada exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SongResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "No se encontraron canciones")
    })
    public ResponseEntity<List<SongResponseDTO>> getAllSongs() {
        List<SongResponseDTO> songs = songService.findAllSongs();
        return ResponseEntity.ok(songs);
    }

    @GetMapping("/search/{id}")
    @Operation(summary = "Buscar canción por ID", description = "Recupera una canción específica utilizando su identificador único")
    @Parameter(name = "id", description = "Identificador único de la canción", required = true, example = "1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Canción encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SongResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Canción no encontrada")
    })
    public ResponseEntity<SongResponseDTO> getSongById(@PathVariable Long id) {
        SongResponseDTO song = songService.searchSongById(id);
        return ResponseEntity.ok(song);
    }

    @PostMapping(value ="/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Analizar metadatos de canción", description = "Extrae y analiza los metadatos de un archivo de música")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metadatos analizados exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SongMetadataResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Error en el análisis del archivo",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
    })
    public ResponseEntity<SongMetadataResponseDTO> analyzeSong(
            @RequestParam("file") MultipartFile songFile) throws IOException {
        SongMetadataResponseDTO metadata = songService.analyzeSong(songFile);
        return ResponseEntity.ok(metadata);
    }

    @PostMapping(value = "/create", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Crear nueva canción", description = "Crea una nueva canción en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Canción creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de canción inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno al crear la canción")
    })
    public ResponseEntity<Void> createSong(
            @Parameter(description = "Archivo de audio de la canción", required = true)
            @RequestParam(value = "songFile") MultipartFile songFile,
            @Parameter(description = "Detalles de la canción", required = true)
            @RequestPart("songRequest") SongRequestDTO songRequestDTO) throws IOException {
        log.info("Creando nueva canción: {}", songRequestDTO.getTitle());
        songService.createSong(songFile, songRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Eliminar canción", description = "Elimina una canción del sistema por su ID")
    @Parameter(name = "id", description = "Identificador único de la canción a eliminar", required = true, example = "1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Canción eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Canción no encontrada")
    })
    public ResponseEntity<Void> deleteSong(@PathVariable Long id) {
        songService.deleteSong(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Actualizar canción", description = "Actualiza los detalles de una canción existente")
    @Parameter(name = "id", description = "Identificador único de la canción a actualizar", required = true, example = "1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Canción actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de actualización inválidos"),
            @ApiResponse(responseCode = "404", description = "Canción no encontrada")
    })
    public ResponseEntity<Void> updateSong(
            @PathVariable Long id,
            @Parameter(description = "Nueva imagen de portada", required = false)
            @RequestParam(value = "coverArt") MultipartFile coverArtFile,
            @Parameter(description = "Detalles actualizados de la canción", required = true)
            @RequestPart("songRequest") SongRequestDTO songRequestDTO) throws IOException {
        songService.updateSong(id, coverArtFile, songRequestDTO);
        return ResponseEntity.ok().build();
    }
}