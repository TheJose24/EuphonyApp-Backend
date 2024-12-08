package com.euphony.streaming.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO para la respuesta de generación de reportes generales.
 */
@Data
@AllArgsConstructor
@Schema(description = "DTO para la respuesta de generación de reportes generales.")
public class ReportGeneralResponseDTO {

    @Schema(description = "Mensaje con el resultado de la operación", example = "Reporte generado con éxito.")
    private String message;

    @Schema(description = "Operación realizada", example = "Generación de reporte")
    private String operation;

    @Schema(description = "Ruta del archivo generado", example = "/reportes/general_report.pdf")
    private String filePath;

}
