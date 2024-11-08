package com.euphony.streaming.controller;

import com.euphony.streaming.dto.request.GenreRequestDTO;
import com.euphony.streaming.dto.response.GenreResponseDTO;
import com.euphony.streaming.service.implementation.GenreServiceImpl;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/genres")
@Tag(name = "Gestión de Géneros", description = "API para la gestión de géneros musicales")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__(@Lazy))
public class GenreController {

    private final GenreServiceImpl genreService;

    @GetMapping("/all")
    @Operation(summary = "Obtener todos los géneros", description = "Retorna la lista de todos los géneros musicales")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operación exitosa", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenreResponseDTO.class))
            })
    })
    public ResponseEntity<List<GenreResponseDTO>> getAllGenres() {
        List<GenreResponseDTO> genres = genreService.findAllGenres();
        return ResponseEntity.ok(genres);
    }


    @GetMapping("/search/{name}")
    @Operation(summary = "Obtener un género por nombre", description = "Retorna un género musical por su nombre")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operación exitosa", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GenreResponseDTO.class))
            }),
            @ApiResponse(responseCode = "404", description = "Género no encontrado", content = {
                    @Content(mediaType = "application/json")
            })
    })
    public ResponseEntity<GenreResponseDTO> getGenreByName(
            @Parameter(description = "Nombre del género") @PathVariable String name) {
        GenreResponseDTO genre = genreService.findGenreByName(name);
        return ResponseEntity.ok(genre);
    }


    @PostMapping("/create")
    @Operation(summary = "Crear un nuevo género", description = "Crea un nuevo género musical")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Género creado", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "409", description = "El género ya existe", content = {
                    @Content(mediaType = "application/json")
            })
    })
    public ResponseEntity<Void> createGenre(
            @Parameter(description = "Datos del nuevo género") @RequestBody GenreRequestDTO genreRequestDTO) {
        genreService.createGenre(genreRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PutMapping("/update/{id}")
    @Operation(summary = "Actualizar un género", description = "Actualiza un género existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Género actualizado", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "404", description = "Género no encontrado", content = {
                    @Content(mediaType = "application/json")
            })
    })
    public ResponseEntity<Void> updateGenre(
            @Parameter(description = "ID del género") @PathVariable Long id,
            @Parameter(description = "Datos actualizados del género") @RequestBody GenreRequestDTO genreRequestDTO) {
        genreService.updateGenre(id, genreRequestDTO);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Eliminar un género", description = "Elimina un género existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Género eliminado", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "404", description = "Género no encontrado", content = {
                    @Content(mediaType = "application/json")
            })
    })
    public ResponseEntity<Void> deleteGenre(
            @Parameter(description = "ID del género") @PathVariable Long id) {
        genreService.deleteGenre(id);
        return ResponseEntity.noContent().build();
    }
}