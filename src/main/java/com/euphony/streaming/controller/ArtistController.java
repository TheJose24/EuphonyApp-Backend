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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Operation(summary = "Crear un nuevo artista",
            description = "Crea un artista con una imagen opcional. Se envía como multipart/form-data: " +
                    "la parte 'artistRequestDTO' (JSON) con los datos y la parte 'imageFile' (archivo) opcional.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Artista creado exitosamente"),
            @ApiResponse(responseCode = "409", description = "El artista ya existe"),
            @ApiResponse(responseCode = "400", description = "Datos de artista inválidos")
    })
    @PostMapping(value = "/create", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> createArtist(
            @Parameter(description = "Datos del artista a crear")
            @RequestPart("artistRequestDTO") ArtistRequestDTO artistRequestDTO,
            @Parameter(description = "Imagen del artista (opcional)")
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        artistService.createArtist(artistRequestDTO, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Actualizar un artista existente",
            description = "Actualiza los datos del artista y, opcionalmente, su imagen. Se envía como " +
                    "multipart/form-data: la parte 'artistRequestDTO' (JSON) y la parte 'imageFile' (archivo) " +
                    "opcional. Si no se envía 'imageFile', se conserva la imagen actual.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Artista actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Artista no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de artista inválidos")
    })
    @PutMapping(value = "/update/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> updateArtist(
            @Parameter(description = "ID del artista a actualizar")
            @PathVariable Long id,
            @Parameter(description = "Nuevos datos del artista")
            @RequestPart("artistRequestDTO") ArtistRequestDTO artistRequestDTO,
            @Parameter(description = "Nueva imagen del artista (opcional)")
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        artistService.updateArtist(id, artistRequestDTO, imageFile);
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