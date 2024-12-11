package com.euphony.streaming.controller;

import com.euphony.streaming.dto.response.PlayHistoryResponseDTO;
import com.euphony.streaming.service.implementation.PlayHistoryServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/play-history")
@Tag(name = "Historial de Reproducciones",
        description = "API para gestionar el historial de reproducciones de usuarios")
@Slf4j
@RequiredArgsConstructor
public class PlayHistoryController {

    private final PlayHistoryServiceImpl playHistoryService;

    @PostMapping("/{userId}/record/{songId}")
    @Operation(
            summary = "Registrar reproducción",
            description = "Registra una nueva reproducción de canción para un usuario"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reproducción registrada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario o canción no encontrada")
    })
    public ResponseEntity<Void> recordPlay(
            @PathVariable UUID userId,
            @PathVariable Long songId) {
        log.info("Registrando reproducción: Usuario {} - Canción {}", userId, songId);
        playHistoryService.recordPlay(userId, songId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{userId}")
    @Operation(
            summary = "Obtener historial",
            description = "Obtiene el historial completo de reproducciones de un usuario"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Historial recuperado exitosamente",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlayHistoryResponseDTO.class)))
    )
    public ResponseEntity<List<PlayHistoryResponseDTO>> getUserPlayHistory(
            @PathVariable UUID userId) {
        log.info("Obteniendo historial para usuario: {}", userId);
        return ResponseEntity.ok(playHistoryService.getUserPlayHistory(userId));
    }

    @GetMapping("/{userId}/recent")
    @Operation(
            summary = "Obtener reproducciones recientes",
            description = "Obtiene las reproducciones más recientes de un usuario"
    )
    public ResponseEntity<List<PlayHistoryResponseDTO>> getRecentPlays(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Obteniendo {} reproducciones recientes para usuario: {}", limit, userId);
        return ResponseEntity.ok(playHistoryService.getRecentPlays(userId, limit));
    }
}
