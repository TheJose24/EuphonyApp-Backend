package com.euphony.streaming.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de respuesta para la entidad Cancion")
public class SongResponseDTO {

    @Schema(description = "ID de la canción", example = "10")
    private Long songId;

    @Schema(description = "ID del artista asociado a la canción", example = "1")
    private Long artistId;

    @Schema(description = "ID del álbum asociado, puede ser nulo si la canción no tiene álbum", example = "2")
    private Long albumId;

    @Schema(description = "Título de la canción", example = "Mi Cancion Favorita")
    private String title;

    @Schema(description = "URL o ruta de la portada de la canción", example = "/imagenes/portada.jpg")
    private String coverImg;

    @Schema(description = "Duración de la canción en formato HH:mm:ss", example = "00:03:45")
    private String duration;

    @Schema(description = "Idioma de la canción", example = "Español")
    private String language;

    @Schema(description = "Letra de la canción", example = "Letra de la canción...")
    private String lyrics;

    @Schema(description = "Fecha de lanzamiento de la canción", example = "2023-11-01")
    private String releaseDate;

    @Schema(description = "Ruta del archivo de audio de la canción", example = "/uploads/audio/cancion.mp3")
    private String filePath;

    @Schema(description = "Calificación promedio de la canción", example = "4.5")
    private BigDecimal averageRating;

    @Schema(description = "Número de reproducciones de la canción", example = "1000")
    private Integer numberOfPlays;

    @Schema(description = "Nombres de los géneros asociados a la canción", example = "[\"Pop\", \"Rock\"]")
    private Set<String> genres;
}
