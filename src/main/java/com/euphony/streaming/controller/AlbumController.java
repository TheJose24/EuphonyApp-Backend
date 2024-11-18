package com.euphony.streaming.controller;

import com.euphony.streaming.dto.request.AlbumRequestDTO;
import com.euphony.streaming.dto.response.AlbumResponseDTO;
import com.euphony.streaming.service.implementation.AlbumServiceImpl;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/albums")
@RequiredArgsConstructor(onConstructor_ = @__(@Lazy))
@Tag(name = "Gestión de Álbumes", description = "API para la administración de álbumes musicales")
public class AlbumController {

    private final AlbumServiceImpl albumService;

    @GetMapping("/all")
    @Operation(summary = "Obtener todos los álbumes",
            description = "Recupera una lista de todos los álbumes en el sistema")
    @ApiResponse(responseCode = "200", description = "Álbumes recuperados exitosamente",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AlbumResponseDTO.class)))
    public ResponseEntity<List<AlbumResponseDTO>> getAllAlbums() {
        List<AlbumResponseDTO> albums = albumService.findAllAlbums();
        return new ResponseEntity<>(albums, HttpStatus.OK);
    }

    @GetMapping("/search/{name}")
    @Operation(summary = "Buscar álbum por nombre",
            description = "Recupera un álbum específico por su nombre")
    @ApiResponse(responseCode = "200", description = "Álbum encontrado exitosamente",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AlbumResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Álbum no encontrado", content = {
            @Content(mediaType = "application/json")
    })
    public ResponseEntity<AlbumResponseDTO> getAlbumByName(
            @Parameter(description = "Nombre del álbum a buscar", required = true)
            @PathVariable String name) {
        AlbumResponseDTO album = albumService.findAlbumByName(name);
        return new ResponseEntity<>(album, HttpStatus.OK);
    }

    @PostMapping(value = "/create", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Crear un nuevo álbum",
            description = "Crea un nuevo álbum con una imagen de portada opcional")
    @ApiResponse(responseCode = "201", description = "Álbum creado exitosamente", content = {
            @Content(mediaType = "application/json")
    })
    @ApiResponse(responseCode = "400", description = "Datos de álbum inválidos", content = {
            @Content(mediaType = "application/json")
    })
    @ApiResponse(responseCode = "409", description = "El álbum ya existe", content = {
            @Content(mediaType = "application/json")
    })
    public ResponseEntity<String> createAlbum(
            @Parameter(description = "Detalles del álbum", required = true)
            @RequestPart("albumRequestDTO") AlbumRequestDTO albumRequestDTO,
            @Parameter(description = "Imagen de portada opcional del álbum")
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImage) {
        albumService.createAlbum(albumRequestDTO, coverImage);
        return new ResponseEntity<>("Álbum creado exitosamente", HttpStatus.CREATED);
    }

    @PutMapping(value = "/update/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Actualizar un álbum existente",
            description = "Actualiza los detalles del álbum y opcionalmente la imagen de portada")
    @ApiResponse(responseCode = "200", description = "Álbum actualizado exitosamente", content = {
            @Content(mediaType = "application/json")
    })
    @ApiResponse(responseCode = "400", description = "Datos de actualización inválidos", content = {
            @Content(mediaType = "application/json")
    })
    @ApiResponse(responseCode = "404", description = "Álbum no encontrado", content = {
            @Content(mediaType = "application/json")
    })
    @ApiResponse(responseCode = "409", description = "Conflicto con los datos del álbum", content = {
            @Content(mediaType = "application/json")
    })
    public ResponseEntity<String> updateAlbum(
            @Parameter(description = "ID del álbum a actualizar", required = true)
            @PathVariable Long id,
            @Parameter(description = "Detalles actualizados del álbum", required = true)
            @RequestPart("albumRequestDTO") AlbumRequestDTO albumRequestDTO,
            @Parameter(description = "Nueva imagen de portada opcional")
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImage) {
        albumService.updateAlbum(id, albumRequestDTO, coverImage);
        return new ResponseEntity<>("Álbum actualizado exitosamente", HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Eliminar un álbum",
            description = "Elimina un álbum del sistema por su ID")
    @ApiResponse(responseCode = "204", description = "Álbum eliminado exitosamente", content = {
            @Content(mediaType = "application/json")
    })
    @ApiResponse(responseCode = "404", description = "Álbum no encontrado", content = {
            @Content(mediaType = "application/json")
    })
    @ApiResponse(responseCode = "500", description = "Error interno al eliminar el álbum", content = {
            @Content(mediaType = "application/json")
    })
    public ResponseEntity<String> deleteAlbum(
            @Parameter(description = "ID del álbum a eliminar", required = true)
            @PathVariable Long id) {
        albumService.deleteAlbum(id);
        return new ResponseEntity<>("Álbum eliminado exitosamente", HttpStatus.NO_CONTENT);
    }
}