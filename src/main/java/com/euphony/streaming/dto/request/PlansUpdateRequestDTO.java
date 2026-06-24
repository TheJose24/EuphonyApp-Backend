package com.euphony.streaming.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de solicitud para la actualización de un plan de suscripción")
public class PlansUpdateRequestDTO {

    @Schema(description = "Tarifa inicial del plan de suscripción", example = "0.00")
    private BigDecimal setupFee;

    @Schema(description = "Nombre del plan de suscripción", example = "Plan Premium")
    @NotNull(message = "El nombre del plan es requerido")
    @NotBlank(message = "El nombre del plan no puede estar vacío")
    private String planName;

    @Schema(description = "Descripción del plan de suscripción", example = "Acceso ilimitado a música sin anuncios.")
    @NotBlank(message = "La descripción del plan no puede estar vacía")
    private String description;
}
