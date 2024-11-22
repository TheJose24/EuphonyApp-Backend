package com.euphony.streaming.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO de respuesta para los metadatos de una canción.")
public class SongMetadataResponseDTO {

    @Schema(description = "Título de la canción", example = "Mi Canción Favorita")
    private String title;

    @Schema(description = "Nombre del artista de la canción", example = "Artista")
    private String artist;

    @Schema(description = "Nombre del álbum de la canción", example = "Mi Album")
    private String album;

    @Schema(description = "Año de lanzamiento de la canción", example = "2023")
    private String releaseDate;

    @Schema(description = "Duración de la canción", example = "00:03:45")
    private String duration;

    @Schema(description = "Géneros asociados a la canción", example = "[\"Pop\", \"Rock\"]")
    private Set<String> genres;

    @Schema(description = "Letra de la canción", example = "Letra de la canción...")
    private String lyrics;

    @Schema(description = "Ruta del archivo de audio de la canción", example = "/uploads/audio/cancion.mp3")
    private String filePath;

    @Schema(description = "Ruta de la imagen del album", example = "/uploads/images/default_cover_art.png")
    private String albumCoverPath;
}
