package com.euphony.streaming.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para creación y actualización de géneros")
public class GenreRequestDTO {

    @Schema(description = "Nombre del género", example = "Rock")
    private String name;

    @Schema(description = "Descripción del género", example = "Género musical que se caracteriza por su ritmo y melodía")
    private String description;

}
