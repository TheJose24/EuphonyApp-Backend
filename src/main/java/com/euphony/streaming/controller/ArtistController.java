package com.euphony.streaming.controller;

import com.euphony.streaming.dto.request.ArtistRequestDTO;
import com.euphony.streaming.dto.response.ArtistResponseDTO;
import com.euphony.streaming.service.implementation.ArtistServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/artists")
@RequiredArgsConstructor(onConstructor_ = @__(@Lazy))
@Tag(name = "Gestión de Artistas", description = "API para la gestión de artistas")
public class ArtistController {

    private final ArtistServiceImpl artistService;

    @Operation(summary = "Obtener todos los artistas")
    @ApiResponse(responseCode = "200", description = "Lista de artistas recuperada exitosamente")
    @GetMapping("/all")
    public ResponseEntity<List<ArtistResponseDTO>> getAllArtists() {
        return ResponseEntity.ok(artistService.findAllArtists());
    }

    @Operation(summary = "Buscar artista por nombre")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Artista encontrado exitosamente", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ArtistResponseDTO.class))
            }),
            @ApiResponse(responseCode = "404", description = "Artista no encontrado", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "400", description = "Nombre de artista inválido", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @GetMapping("/search/{name}")
    public ResponseEntity<ArtistResponseDTO> getArtistByName(
            @Parameter(description = "Nombre del artista a buscar")
            @PathVariable String name) {
        return ResponseEntity.ok(artistService.findArtistByName(name));
    }

    @Operation(summary = "Crear un nuevo artista")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Artista creado exitosamente"),
            @ApiResponse(responseCode = "409", description = "El artista ya existe"),
            @ApiResponse(responseCode = "400", description = "Datos de artista inválidos")
    })
    @PostMapping("/create")
    public ResponseEntity<Void> createArtist(
            @Parameter(description = "Datos del artista a crear")
            @RequestBody ArtistRequestDTO artistRequestDTO) {
        artistService.createArtist(artistRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Actualizar un artista existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Artista actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Artista no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de artista inválidos")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<Void> updateArtist(
            @Parameter(description = "ID del artista a actualizar")
            @PathVariable Long id,
            @Parameter(description = "Nuevos datos del artista")
            @RequestBody ArtistRequestDTO artistRequestDTO) {
        artistService.updateArtist(id, artistRequestDTO);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Eliminar un artista")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Artista eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Artista no encontrado")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteArtist(
            @Parameter(description = "ID del artista a eliminar")
            @PathVariable Long id) {
        artistService.deleteArtist(id);
        return ResponseEntity.noContent().build();
    }
}