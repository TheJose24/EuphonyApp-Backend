package com.euphony.streaming.dto.response;

import com.euphony.streaming.util.enums.PlanInterval;
import com.euphony.streaming.util.enums.SubscriptionFeature;
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
@Schema(description = "DTO de respuesta que contiene la información de un plan de suscripción")
public class PlansSubscriptionResponseDTO {

    @Schema(description = "ID del plan de suscripción", example = "1")
    private Long planId;

    @Schema(description = "Intervalo de facturación del plan", example = "MONTH")
    private PlanInterval interval;

    @Schema(description = "Funcionalidades del plan de suscripción", example = "['AD_FREE', 'UNLIMITED_SKIPS']")
    private Set<SubscriptionFeature> features;

    @Schema(description = "Tarifa inicial del plan de suscripción", example = "0.00")
    private BigDecimal setupFee;

    @Schema(description = "Nombre del plan de suscripción", example = "Plan Premium")
    private String planName;

    @Schema(description = "Precio del plan de suscripción", example = "19.99")
    private BigDecimal price;

    @Schema(description = "Duración del plan en meses", example = "12")
    private Integer duration;

    @Schema(description = "Descripción del plan de suscripción", example = "Acceso ilimitado a música sin anuncios.")
    private String description;

    @Schema(description = "Indica si el plan está activo", example = "true")
    private Boolean isActive;

    @Schema(description = "Indica si el plan de suscripción tiene un periodo de prueba gratuito", example = "true")
    private Boolean freeTrial;

    @Schema(description = "Duración del periodo de prueba gratuito en dias", example = "7")
    private Integer durationFreeTrial;
}
