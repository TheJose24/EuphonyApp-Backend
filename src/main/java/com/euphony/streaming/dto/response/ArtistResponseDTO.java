package com.euphony.streaming.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de respuesta que contiene la información de un artista")
public class ArtistResponseDTO {

    @Schema(description = "ID del artista", example = "1")
    private Long idArtist;

    @Schema(description = "Nombre del artista", example = "John Doe")
    private String name;

    @Schema(description = "Biografía del artista")
    private String biography;

    @Schema(description = "País de origen del artista", example = "USA")
    private String country;

    @Schema(description = "Redes sociales del artista", example = "{\"Twitter\": \"@johndoe\", \"Instagram\": \"johndoe\"}")
    private Map<String, String> socialNetworks;

    @Schema(description = "Indica si el artista está verificado", example = "false")
    private Boolean isVerified;
}