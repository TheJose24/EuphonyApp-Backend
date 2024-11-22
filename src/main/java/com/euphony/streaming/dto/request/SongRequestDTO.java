package com.euphony.streaming.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de solicitud para la creación y actualización de una canción.")
public class SongRequestDTO {

    @Schema(description = "Título de la canción", example = "Mi Canción Favorita")
    private String title;

    @Schema(description = "Nombre del artista de la canción", example = "Artista")
    private String artist;

    @Schema(description = "Nombre del álbum de la canción", example = "Mi Album")
    private String album;

    @Schema(description = "Año de lanzamiento de la canción", example = "2023")
    private String releaseDate;

    @Schema(description = "Idioma de la canción", example = "Español")
    private String language;

    @Schema(description = "Duración de la canción en formato HH:mm:ss", example = "00:03:45")
    private String duration;

    @Schema(description = "Géneros asociados a la canción", example = "[\"Pop\", \"Rock\"]")
    private Set<String> genres;

    @Schema(description = "Letra de la canción", example = "Letra de la canción...")
    private String lyrics;

    @Schema(description = "Ruta de la imagen del album", example = "/uploads/images/default_cover_art.png")
    private String albumCoverPath;

}
