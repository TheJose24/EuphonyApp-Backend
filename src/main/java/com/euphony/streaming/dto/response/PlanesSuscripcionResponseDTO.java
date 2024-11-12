package com.euphony.streaming.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de respuesta que contiene la información de un plan de suscripción")
public class PlanesSuscripcionResponseDTO {

    @Schema(description = "ID del plan de suscripción", example = "1")
    private Long planId;

    @Schema(description = "Nombre del plan de suscripción", example = "Plan Premium")
    private String planName;

    @Schema(description = "Precio del plan de suscripción", example = "19.99")
    private Double price;

    @Schema(description = "Duración del plan en meses", example = "12")
    private Integer duration;

    @Schema(description = "Descripción del plan de suscripción", example = "Acceso ilimitado a música sin anuncios.")
    private String description;

    @Schema(description = "Indica si el plan está activo", example = "true")
    private Boolean isActive;
}
