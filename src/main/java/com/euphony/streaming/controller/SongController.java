package com.euphony.streaming.controller;

import com.euphony.streaming.dto.request.SongRequestDTO;
import com.euphony.streaming.dto.response.SongMetadataResponseDTO;
import com.euphony.streaming.dto.response.SongResponseDTO;
import com.euphony.streaming.service.interfaces.ISongService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/v1/songs")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Gestión de Canciones",
        description = "API para la gestión completa de canciones en la plataforma Euphony"
)
public class SongController {

    private static final String BYTES_RANGE_FORMAT = "bytes %d-%d/%d";
    private static final int BUFFER_SIZE = 8192;
    private static final String AUDIO_MPEG = "audio/mpeg";
    private static final String BYTES_UNIT = "bytes";
    private static final String INLINE_FILENAME = "inline; filename=\"%s\"";

    private final ISongService songService;

    @GetMapping("/all")
    @Operation(
            summary = "Listar canciones",
            description = "Recupera el listado completo de canciones disponibles en la plataforma"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Listado de canciones recuperado exitosamente",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = SongResponseDTO.class))
            )
    )
    public ResponseEntity<List<SongResponseDTO>> getAllSongs() {
        log.debug("REST request to get all Songs");
        List<SongResponseDTO> songs = songService.findAllSongs();
        return songs.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(songs);
    }

    @GetMapping("/search/{id}")
    @Operation(
            summary = "Obtener canción por ID",
            description = "Recupera una canción específica mediante su identificador único"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Canción encontrada exitosamente",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SongResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Canción no encontrada",
            content = @Content
    )
    public ResponseEntity<SongResponseDTO> getSongById(
            @Parameter(description = "ID de la canción", required = true)
            @PathVariable Long id
    ) {
        log.debug("REST request to get Song : {}", id);
        return ResponseEntity.ok(songService.searchSongById(id));
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Analizar metadatos",
            description = "Analiza y extrae los metadatos de un archivo de audio"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Metadatos analizados correctamente",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SongMetadataResponseDTO.class)
            )
    )
    public ResponseEntity<SongMetadataResponseDTO> analyzeSong(
            @Parameter(description = "Archivo de audio a analizar", required = true)
            @RequestParam("songFile") MultipartFile songFile) throws IOException {
        log.debug("REST request to analyze song file: {}", songFile.getOriginalFilename());
        validateMultipartFile(songFile);
        return ResponseEntity.ok(songService.analyzeSong(songFile));
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Crear canción",
            description = "Registra una nueva canción en la plataforma"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Canción creada exitosamente"
    )
    public void createSong(
            @Parameter(description = "Archivo de audio", required = true)
            @RequestParam("songFile") MultipartFile songFile,
            @Parameter(description = "Datos de la canción", required = true)
            @RequestPart("songRequest") SongRequestDTO songRequestDTO) throws IOException {
        log.debug("REST request to save Song : {}", songRequestDTO);
        validateMultipartFile(songFile);
        log.info("Creating new song: {}", songRequestDTO.getTitle());
        songService.createSong(songFile, songRequestDTO);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Eliminar canción",
            description = "Elimina una canción existente de la plataforma"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Canción eliminada exitosamente"
    )
    public void deleteSong(
            @Parameter(description = "ID de la canción a eliminar", required = true)
            @PathVariable Long id
    ) {
        log.debug("REST request to delete Song : {}", id);
        songService.deleteSong(id);
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Actualizar canción",
            description = "Actualiza los datos de una canción existente"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Canción actualizada exitosamente"
    )
    public ResponseEntity<Void> updateSong(
            @Parameter(description = "ID de la canción", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nueva portada", required = false)
            @RequestParam(value = "coverArt", required = false) MultipartFile coverArt,
            @Parameter(description = "Datos actualizados", required = true)
            @RequestPart("songRequest") SongRequestDTO songRequestDTO) throws IOException {
        log.debug("REST request to update Song : {}", songRequestDTO);
        if (coverArt != null && !coverArt.isEmpty()) {
            validateMultipartFile(coverArt);
        }
        songService.updateSong(id, coverArt, songRequestDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stream/{id}")
    @Operation(summary = "Stream song", description = "Streams a song with support for range requests")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Streaming started successfully"),
            @ApiResponse(responseCode = "206", description = "Partial content streaming"),
            @ApiResponse(responseCode = "404", description = "Song not found")
    })
    public ResponseEntity<StreamingResponseBody> streamSong(
            @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String rangeHeader) throws IOException {

        Path songPath = songService.getSongFilePath(id);
        validateSongExists(songPath);

        StreamingDetails streamingDetails = calculateStreamingDetails(songPath, rangeHeader);
        StreamingResponseBody responseStream = createStreamingResponseBody(songPath, streamingDetails);

        return ResponseEntity
                .status(rangeHeader != null ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK)
                .contentType(MediaType.parseMediaType(AUDIO_MPEG))
                .headers(createStreamingHeaders(songPath, streamingDetails))
                .body(responseStream);
    }

    private void validateMultipartFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
    }

    private void validateSongExists(Path songPath) throws IOException {
        if (!Files.exists(songPath)) {
            throw new IOException("Song file not found");
        }
    }

    private StreamingDetails calculateStreamingDetails(Path songPath, String rangeHeader) throws IOException {
        long fileSize = Files.size(songPath);
        long rangeStart = 0;
        long rangeEnd = fileSize - 1;

        if (rangeHeader != null && !rangeHeader.isEmpty()) {
            String[] ranges = rangeHeader.substring(BYTES_UNIT.length() + 1).split("-");
            rangeStart = Long.parseLong(ranges[0]);
            if (ranges.length > 1) {
                rangeEnd = Long.parseLong(ranges[1]);
            }
        }

        return new StreamingDetails(rangeStart, rangeEnd, fileSize);
    }

    private StreamingResponseBody createStreamingResponseBody(Path songPath, StreamingDetails details) {
        return outputStream -> {
            try (InputStream inputStream = Files.newInputStream(songPath);
                 BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {

                bufferedInputStream.skip(details.rangeStart());
                streamContent(bufferedInputStream, outputStream, details.contentLength());
            }
        };
    }

    private void streamContent(BufferedInputStream input, OutputStream output, long contentLength) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        long bytesRemaining = contentLength;
        int bytesRead;

        while (bytesRemaining > 0 && (bytesRead = input.read(buffer, 0,
                (int) Math.min(buffer.length, bytesRemaining))) != -1) {
            output.write(buffer, 0, bytesRead);
            bytesRemaining -= bytesRead;
        }
        output.flush();
    }

    private HttpHeaders createStreamingHeaders(Path songPath, StreamingDetails details) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                String.format(INLINE_FILENAME, songPath.getFileName().toString()));
        headers.add(HttpHeaders.ACCEPT_RANGES, BYTES_UNIT);
        headers.add(HttpHeaders.CONTENT_RANGE,
                String.format(BYTES_RANGE_FORMAT, details.rangeStart(), details.rangeEnd(), details.fileSize()));
        headers.setContentLength(details.contentLength());
        return headers;
    }

    private record StreamingDetails(long rangeStart, long rangeEnd, long fileSize) {
        public long contentLength() {
            return rangeEnd - rangeStart + 1;
        }
    }
}