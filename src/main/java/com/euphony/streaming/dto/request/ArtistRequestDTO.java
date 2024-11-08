package com.euphony.streaming.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de solicitud para creación y actualización un artista")
public class ArtistRequestDTO {

    @Schema(description = "Nombre del artista", example = "John Doe")
    @NotNull(message = "El nombre del artista es requerido")
    @NotBlank(message = "El nombre del artista no puede estar vacío")
    private String name;

    @Schema(description = "Biografía del artista")
    private String biography;

    @Schema(description = "País de origen del artista", example = "USA")
    @NotNull(message = "El país del artista es requerido")
    @NotBlank(message = "El país del artista no puede estar vacío")
    private String country;

    @Schema(description = "Redes sociales del artista", example = "{\"Twitter\": \"@johndoe\", \"Instagram\": \"johndoe\"}")
    private Map<String, String> socialNetworks;

}