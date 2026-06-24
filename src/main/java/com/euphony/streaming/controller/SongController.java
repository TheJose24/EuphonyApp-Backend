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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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



    @GetMapping("/stream/{id}")
    @Operation(summary = "Reproducir canción", description = "Transmite una canción en streaming")
    @Parameter(name = "id", description = "Identificador único de la canción a reproducir", required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Streaming iniciado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Canción no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error al procesar el streaming")
    })
    public ResponseEntity<StreamingResponseBody> streamSong(
            @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {

        try {
            // Obtener el path del archivo
            Path songPath = songService.getSongFilePath(id);
            if (!Files.exists(songPath)) {
                log.error("Canción no encontrada: {}", id);
                return ResponseEntity.notFound().build();
            }

            // Obtener el tamaño del archivo
            long fileSize = Files.size(songPath);

            // Procesar el header Range si existe
            long rangeStart;
            long rangeEnd = fileSize - 1;

            if (rangeHeader != null && !rangeHeader.isEmpty()) {
                String[] ranges = rangeHeader.substring("bytes=".length()).split("-");
                rangeStart = Long.parseLong(ranges[0]);
                if (ranges.length > 1) {
                    rangeEnd = Long.parseLong(ranges[1]);
                }
            } else {
                rangeStart = 0;
            }

            // Calcular el tamaño del contenido a enviar
            long contentLength = rangeEnd - rangeStart + 1;

            // Crear el StreamingResponseBody
            StreamingResponseBody responseStream = outputStream -> {
                try (InputStream inputStream = Files.newInputStream(songPath);
                     BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {

                    // Saltar hasta el inicio del rango solicitado
                    bufferedInputStream.skip(rangeStart);

                    // Buffer para la transmisión
                    byte[] buffer = new byte[8192];
                    long bytesRemaining = contentLength;
                    int bytesRead;

                    while (bytesRemaining > 0 && (bytesRead = bufferedInputStream.read(buffer, 0,
                            (int) Math.min(buffer.length, bytesRemaining))) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        bytesRemaining -= bytesRead;
                    }

                    outputStream.flush();
                }
            };

            // Construir la respuesta con los headers apropiados
            return ResponseEntity.status(rangeHeader != null ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK)
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + songPath.getFileName().toString() + "\"")
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_RANGE, String.format("bytes %d-%d/%d", rangeStart, rangeEnd, fileSize))
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                    .body(responseStream);

        } catch (IOException e) {
            log.error("Error al procesar el streaming de la canción: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}