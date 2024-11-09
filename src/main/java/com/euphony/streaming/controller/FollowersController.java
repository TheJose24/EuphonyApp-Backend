package com.euphony.streaming.controller;

import com.euphony.streaming.dto.request.FollowersArtistRequestDTO;
import com.euphony.streaming.dto.response.FollowersArtistResponseDTO;
import com.euphony.streaming.service.interfaces.IFollowersService;
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

/**
 * Controlador REST que gestiona las operaciones relacionadas con los seguidores de artistas.
 */
@RestController
@RequestMapping("/api/v1/followers")
@Tag(name = "Gestión de Seguidores", description = "API para gestionar seguidores de artistas")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__(@Lazy))
@Validated
public class FollowersController {

    private final IFollowersService followersService;

    @Operation(
            summary = "Seguir a un artista",
            description = "Permite a un usuario seguir a un artista específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario ahora sigue al artista"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud inválida"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario o artista no encontrado"
            )
    })
    @PostMapping("/follow")
    public ResponseEntity<FollowersArtistResponseDTO> followArtist(
            @RequestBody @Validated FollowersArtistRequestDTO request
    ) {
        log.debug("REST request para seguir artista: {}", request);
        followersService.followArtist(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Dejar de seguir a un artista",
            description = "Permite a un usuario dejar de seguir a un artista específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario ha dejado de seguir al artista"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud inválida"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario o artista no encontrado"
            )
    })
    @DeleteMapping("/unfollow")
    public ResponseEntity<FollowersArtistResponseDTO> unfollowArtist(
            @RequestBody @Validated FollowersArtistRequestDTO request
    ) {
        log.debug("REST request para dejar de seguir artista: {}", request);
        followersService.unfollowArtist(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Obtener seguidores de un artista",
            description = "Recupera la lista de usuarios que siguen a un artista específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de seguidores recuperada exitosamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FollowersArtistResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Artista no encontrado",
                    content = @Content
            )
    })
    @GetMapping("/by-artist/{artistId}")
    public ResponseEntity<List<FollowersArtistResponseDTO>> getFollowersByArtist(
            @Parameter(description = "ID del artista", required = true)
            @PathVariable Long artistId
    ) {
        log.debug("REST request para obtener seguidores del artista ID: {}", artistId);
        List<FollowersArtistResponseDTO> followers = followersService.getFollowersByArtist(artistId);
        return ResponseEntity.ok(followers);
    }

    @Operation(
            summary = "Obtener artistas seguidos por un usuario",
            description = "Recupera la lista de artistas que un usuario específico sigue"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de artistas seguidos recuperada exitosamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FollowersArtistResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            )
    })
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<FollowersArtistResponseDTO>> getFollowersByUser(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable UUID userId
    ) {
        log.debug("REST request para obtener artistas seguidos por el usuario ID: {}", userId);
        List<FollowersArtistResponseDTO> following = followersService.getFollowersByUser(userId);
        return ResponseEntity.ok(following);
    }
}