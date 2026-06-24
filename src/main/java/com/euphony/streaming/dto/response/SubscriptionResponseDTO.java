package com.euphony.streaming.dto.response;

import com.euphony.streaming.util.enums.SubscriptionState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de respuesta que contiene la información de la suscripción.")
public class SubscriptionResponseDTO {

    private Long idSuscripcion;

    private UUID idUsuario;

    private Long idPlan;

    private Long idMetodoPago;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaRenovacion;

    private LocalDateTime fechaCancelacion;

    private SubscriptionState estado;
}
