package com.euphony.streaming.service.interfaces;

import java.io.IOException;
import java.util.List;

/**
 * Interfaz para generar reportes Excel de diferentes entidades.
 */
public interface IReportGeneratorService {

    /**
     * Genera un reporte Excel para una entidad específica.
     *
     * @param entityName Nombre de la entidad (por ejemplo, "Users", "Playlists").
     * @param requestData Datos de la entidad en forma de lista.
     * @return Ruta del archivo generado.
     * @throws IOException Si ocurre un error al generar el reporte.
     */
    String generateReport(String entityName, Object requestData) throws IOException;

    /**
     * Convierte los datos proporcionados a una lista si no lo son ya.
     *
     * @param requestData Datos a convertir.
     * @return Lista de datos.
     */
    List<?> convertToList(Object requestData);
}
