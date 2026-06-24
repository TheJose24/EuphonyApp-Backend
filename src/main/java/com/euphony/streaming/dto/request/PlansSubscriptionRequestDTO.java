package com.euphony.streaming.dto.request;

import com.euphony.streaming.util.enums.PlanInterval;
import com.euphony.streaming.util.enums.SubscriptionFeature;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
@Schema(description = "DTO de solicitud para la creación de un plan de suscripción")
public class PlansSubscriptionRequestDTO {

    @Schema(description = "Intervalo de facturación del plan", example = "MONTH")
    @NotNull(message = "El intervalo de facturación es requerido")
    @NotBlank(message = "El intervalo de facturación no puede estar vacío")
    private PlanInterval interval;

    @Schema(description = "Funcionalidades del plan de suscripción", example = "['NO_ADS', 'PREMIUM_ACCOUNT']")
    private Set<SubscriptionFeature> features;

    @Schema(description = "Tarifa inicial del plan de suscripción", example = "0.00")
    private BigDecimal setupFee;

    @Schema(description = "Nombre del plan de suscripción", example = "Plan Premium")
    @NotNull(message = "El nombre del plan es requerido")
    @NotBlank(message = "El nombre del plan no puede estar vacío")
    private String planName;

    @Schema(description = "Precio del plan de suscripción", example = "19.99")
    @NotNull(message = "El precio del plan es requerido")
    @Positive(message = "El precio del plan debe ser un valor positivo")
    private BigDecimal price;

    @Schema(description = "Duración del plan en meses", example = "12")
    @NotNull(message = "La duración del plan es requerida")
    @Positive(message = "La duración debe ser un valor positivo")
    private Integer duration;

    @Schema(description = "Descripción del plan de suscripción", example = "Acceso ilimitado a música sin anuncios.")
    @NotBlank(message = "La descripción del plan no puede estar vacía")
    private String description;

    @Schema(description = "Indica si el plan está activo", example = "true")
    private Boolean isActive = true;

    @Schema(description = "Indica si el plan de suscripción tiene un periodo de prueba gratuito", example = "true")
    private Boolean freeTrial;

    @Schema(description = "Duración del periodo de prueba gratuito en dias", example = "7")
    private Integer durationFreeTrial;
}
