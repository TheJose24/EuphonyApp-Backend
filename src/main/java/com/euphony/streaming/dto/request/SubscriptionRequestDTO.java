package com.euphony.streaming.dto.request;

import com.euphony.streaming.util.enums.SubscriptionState;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Schema(description = "DTO de solicitud para la creación de una suscripción.")
public class SubscriptionRequestDTO {

    @NotNull(message = "El ID del usuario es obligatorio.")
    private UUID idUsuario;

    @NotNull(message = "El ID del plan es obligatorio.")
    private Long idPlan;

    @NotNull(message = "El ID del método de pago es obligatorio.")
    private Long idMetodoPago;

    @NotNull(message = "La fecha de inicio es obligatoria.")
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha de renovación es obligatoria.")
    private LocalDateTime fechaRenovacion;

    private LocalDateTime fechaCancelacion;

    private SubscriptionState estado;
}
