package com.euphony.streaming.controller;

import com.euphony.streaming.dto.response.*;
import com.euphony.streaming.service.implementation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(
        name = "Reportes",
        description = "API para la generación de reportes en formato Excel de las diferentes entidades del sistema"
)
@Slf4j
@RequiredArgsConstructor
public class ReportGeneratorController {

    private final ReportGeneratorServiceImpl reportGeneratorService;
    private final UserServiceImpl userService;
    private final ArtistServiceImpl artistService;
    private final AlbumServiceImpl albumService;
    private final GenreServiceImpl genreService;
    private final PlaylistServiceImpl playlistService;
    private final ProfileUserServiceImpl userProfileService;

    @Operation(
            summary = "Generar reporte de usuarios",
            description = "Genera un archivo Excel con el listado completo de usuarios registrados en el sistema. " +
                    "Incluye información como nombre, email, fecha de registro y rol."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reporte generado exitosamente",
                    content = @Content(
                            mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizado - Se requiere autenticación",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Prohibido - No tiene permisos suficientes",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor al generar el reporte",
                    content = @Content
            )
    })
    @GetMapping("/users")
    public ResponseEntity<Resource> generateUsersReport() {
        List<UserResponseDTO> users = userService.getUsers();
        Resource reportResource = reportGeneratorService.generateReport("Users", users);
        String filename = String.format("%s_Report.xlsx", "Users");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(reportResource);
    }

    @Operation(
            summary = "Generar reporte de playlists",
            description = "Genera un archivo Excel con todas las playlists del sistema. " +
                    "Incluye nombre, creador, fecha de creación y número de canciones."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reporte generado exitosamente",
                    content = @Content(
                            mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizado - Se requiere autenticación",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Prohibido - No tiene permisos suficientes",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor al generar el reporte",
                    content = @Content
            )
    })
    @GetMapping("/playlists")
    public ResponseEntity<Resource> generatePlaylistsReport() {
        List<PlaylistResponseDTO> playlists = playlistService.findAllPlaylists();
        Resource reportResource = reportGeneratorService.generateReport("Playlists", playlists);
        String filename = String.format("%s_Report.xlsx", "Playlists");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(reportResource);
    }

    @Operation(
            summary = "Generar reporte de géneros musicales",
            description = "Genera un archivo Excel con todos los géneros musicales registrados. " +
                    "Incluye nombre del género y descripción."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reporte generado exitosamente",
                    content = @Content(
                            mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizado - Se requiere autenticación",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Prohibido - No tiene permisos suficientes",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor al generar el reporte",
                    content = @Content
            )
    })
    @GetMapping("/genres")
    public ResponseEntity<Resource> generateGenresReport() {
        List<GenreResponseDTO> genres = genreService.findAllGenres();
        Resource reportResource = reportGeneratorService.generateReport("Genres", genres);
        String filename = String.format("%s_Report.xlsx", "Genres");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(reportResource);
    }

    @Operation(
            summary = "Generar reporte de artistas",
            description = "Genera un archivo Excel con todos los artistas registrados. " +
                    "Incluye nombre artístico, biografía, género principal y número de álbumes."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reporte generado exitosamente",
                    content = @Content(
                            mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizado - Se requiere autenticación",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Prohibido - No tiene permisos suficientes",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor al generar el reporte",
                    content = @Content
            )
    })
    @GetMapping("/artists")
    public ResponseEntity<Resource> generateArtistsReport() {
        List<ArtistResponseDTO> artists = artistService.findAllArtists();
        Resource reportResource = reportGeneratorService.generateReport("Artists", artists);
        String filename = String.format("%s_Report.xlsx", "Artists");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(reportResource);
    }

    @Operation(
            summary = "Generar reporte de álbumes",
            description = "Genera un archivo Excel con todos los álbumes registrados. " +
                    "Incluye título, artista, año de lanzamiento, género y número de canciones."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reporte generado exitosamente",
                    content = @Content(
                            mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizado - Se requiere autenticación",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Prohibido - No tiene permisos suficientes",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor al generar el reporte",
                    content = @Content
            )
    })
    @GetMapping("/albums")
    public ResponseEntity<Resource> generateAlbumsReport() {
        List<AlbumResponseDTO> albums = albumService.findAllAlbums();
        Resource reportResource = reportGeneratorService.generateReport("Albums", albums);
        String filename = String.format("%s_Report.xlsx", "Albums");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(reportResource);
    }

    @Operation(
            summary = "Generar reporte de perfiles de usuario",
            description = "Genera un archivo Excel con todos los perfiles de usuario. " +
                    "Incluye información detallada sobre preferencias musicales y configuración."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reporte generado exitosamente",
                    content = @Content(
                            mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizado - Se requiere autenticación",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Prohibido - No tiene permisos suficientes",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor al generar el reporte",
                    content = @Content
            )
    })
    @GetMapping("/user-profiles")
    public ResponseEntity<Resource> generateUserProfilesReport() {
        List<UserProfileResponseDTO> userProfiles = userProfileService.findAllProfiles();
        Resource reportResource = reportGeneratorService.generateReport("UserProfiles", userProfiles);
        String filename = String.format("%s_Report.xlsx", "UserProfiles");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(reportResource);
    }
}
