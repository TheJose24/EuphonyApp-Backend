package com.euphony.streaming.controller;

import com.euphony.streaming.dto.request.*;
import com.euphony.streaming.dto.response.*;
import com.euphony.streaming.service.implementation.ReportGeneratorServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Gestión de Reportes", description = "API para la generación de reportes por entidad")
@Slf4j
@RequiredArgsConstructor
public class ReportGeneratorController {

    private final ReportGeneratorServiceImpl reportGeneratorService;

    /**
     * Método genérico para la generación de reportes.
     */
    private <T> ResponseEntity<ReportGeneralResponseDTO> generateReport(String entityName, T requestData) {
        // Asegurarse de que los datos sean una lista
        List<T> dataList = (requestData instanceof List<?>)
                ? (List<T>) requestData
                : Collections.singletonList(requestData);

        log.info("Generando reporte para la entidad: {}", entityName);

        // Llamar al servicio para generar el reporte
        String filePath = reportGeneratorService.generateReport(entityName, dataList);

        log.info("Reporte generado correctamente para la entidad: {} en {}", entityName, filePath);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ReportGeneralResponseDTO(
                "Reporte generado correctamente.",
                entityName,
                filePath
        ));
    }
    @Operation(summary = "Generar reporte de usuarios", description = "Genera un reporte con los datos de usuarios")
    @ApiResponse(responseCode = "201", description = "Reporte generado exitosamente",
            content = @Content(schema = @Schema(implementation = ReportGeneralResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Solicitud incorrecta debido a datos inválidos o faltantes",
            content = @Content(schema = @Schema(implementation = Map.class)))
    @PostMapping("/users")
    public ResponseEntity<ReportGeneralResponseDTO> generateUsersReport(@Parameter(description = "Datos de usuarios para generar el reporte") @Valid @RequestBody UserRequestDTO userRequestDTO) {
        return generateReport("Users", userRequestDTO);
    }

    @Operation(summary = "Generar reporte de playlists", description = "Genera un reporte con los datos de playlists")
    @ApiResponse(responseCode = "201", description = "Reporte generado exitosamente",
            content = @Content(schema = @Schema(implementation = ReportGeneralResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Solicitud incorrecta debido a datos inválidos o faltantes",
            content = @Content(schema = @Schema(implementation = Map.class)))
    @PostMapping("/playlists")
    public ResponseEntity<ReportGeneralResponseDTO> generatePlaylistsReport(@Parameter(description = "Datos de playlists para generar el reporte") @Valid @RequestBody PlaylistRequestDTO playlistRequestDTO) {
        return generateReport("Playlists", playlistRequestDTO);
    }

    @Operation(summary = "Generar reporte de géneros", description = "Genera un reporte con los datos de géneros")
    @ApiResponse(responseCode = "201", description = "Reporte generado exitosamente",
            content = @Content(schema = @Schema(implementation = ReportGeneralResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Solicitud incorrecta debido a datos inválidos o faltantes",
            content = @Content(schema = @Schema(implementation = Map.class)))
    @PostMapping("/genres")
    public ResponseEntity<ReportGeneralResponseDTO> generateGenresReport(@Parameter(description = "Datos de géneros para generar el reporte") @Valid @RequestBody GenreRequestDTO genreRequestDTO) {
        return generateReport("Genres", genreRequestDTO);
    }

    @Operation(summary = "Generar reporte de artistas", description = "Genera un reporte con los datos de artistas")
    @ApiResponse(responseCode = "201", description = "Reporte generado exitosamente",
            content = @Content(schema = @Schema(implementation = ReportGeneralResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Solicitud incorrecta debido a datos inválidos o faltantes",
            content = @Content(schema = @Schema(implementation = Map.class)))
    @PostMapping("/artists")
    public ResponseEntity<ReportGeneralResponseDTO> generateArtistsReport(@Parameter(description = "Datos de artistas para generar el reporte") @Valid @RequestBody ArtistRequestDTO artistRequestDTO) {
        return generateReport("Artists", artistRequestDTO);
    }

    @Operation(summary = "Generar reporte de álbumes", description = "Genera un reporte con los datos de álbumes")
    @ApiResponse(responseCode = "201", description = "Reporte generado exitosamente",
            content = @Content(schema = @Schema(implementation = ReportGeneralResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Solicitud incorrecta debido a datos inválidos o faltantes",
            content = @Content(schema = @Schema(implementation = Map.class)))
    @PostMapping("/albums")
    public ResponseEntity<ReportGeneralResponseDTO> generateAlbumsReport(@Parameter(description = "Datos de álbumes para generar el reporte") @Valid @RequestBody AlbumRequestDTO albumRequestDTO) {
        return generateReport("Albums", albumRequestDTO);
    }

    @Operation(summary = "Generar reporte de perfiles de usuario", description = "Genera un reporte con los datos de perfiles de usuario")
    @ApiResponse(responseCode = "201", description = "Reporte generado exitosamente",
            content = @Content(schema = @Schema(implementation = ReportGeneralResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Solicitud incorrecta debido a datos inválidos o faltantes",
            content = @Content(schema = @Schema(implementation = Map.class)))
    @PostMapping("/user-profiles")
    public ResponseEntity<ReportGeneralResponseDTO> generateUserProfilesReport(@Parameter(description = "Datos de perfiles de usuario para generar el reporte") @Valid @RequestBody UserProfileRequestDTO userProfileRequestDTO) {
        return generateReport("UserProfiles", userProfileRequestDTO);
    }
}
