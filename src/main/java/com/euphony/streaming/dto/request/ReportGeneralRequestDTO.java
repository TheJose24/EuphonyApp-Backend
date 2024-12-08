package com.euphony.streaming.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para la solicitud de generación de reportes generales.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para la solicitud de generación de reportes generales.")
public class ReportGeneralRequestDTO {

    @Schema(description = "Nombre del reporte", example = "General Report")
    @NotBlank(message = "El nombre del reporte no debe estar vacío")
    private String reportName;

    @Schema(description = "Tipo de entidad a reportar", example = "Users")
    @NotBlank(message = "El tipo de entidad no debe estar vacío")
    private String entityType;

    @Schema(description = "Datos para generar el reporte", example = "[{\"username\": \"john\", \"email\": \"john@example.com\"}]")
    @NotNull(message = "Los datos no pueden ser nulos")
    private List<?> data;  // Cambiar Object a List<?>
}
