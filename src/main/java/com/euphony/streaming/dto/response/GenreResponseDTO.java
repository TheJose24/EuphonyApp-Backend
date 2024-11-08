package com.euphony.streaming.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO de respuesta que contiene la información del genero musical.")
public class GenreResponseDTO {

        @Schema(description = "ID único del género", example = "1")
        private Long idGenre;

        @Schema(description = "Nombre del género", example = "Rock")
        private String name;

        @Schema(description = "Descripción del género", example = "Género musical que se caracteriza por su ritmo y melodía")
        private String description;
}
