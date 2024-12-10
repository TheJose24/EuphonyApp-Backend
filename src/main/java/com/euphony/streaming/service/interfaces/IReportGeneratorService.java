package com.euphony.streaming.service.interfaces;

import com.euphony.streaming.exception.custom.report.ReportGenerationException;
import org.springframework.core.io.Resource;

/**
 * Interfaz que define las operaciones para la generación de reportes Excel.
 * Proporciona funcionalidad para crear reportes dinámicos basados en diferentes
 * entidades del sistema Euphony Streaming.
 *
 * @author Euphony Development Team
 * @version 1.0
 * @since 2024-12-10
 */
public interface IReportGeneratorService {

    /**
     * Genera un reporte Excel personalizado para una entidad específica del sistema.
     * El reporte incluirá un encabezado con el logo de Euphony, título, fecha de generación
     * y los datos de la entidad organizados en una tabla con formato profesional.
     *
     * @param entityName Nombre de la entidad para la cual se generará el reporte.
     *                  Ejemplos válidos incluyen "Users", "Playlists", "Songs", etc.
     *                  No debe ser null ni estar vacío.
     * 
     * @param requestData Datos que se incluirán en el reporte. Puede ser una lista de objetos
     *                   o un objeto individual que será convertido a lista.
     *                   Los objetos deben tener propiedades accesibles mediante reflexión.
     *                   No debe ser null.
     *
     * @return Resource Objeto Resource de Spring que contiene el archivo Excel generado.
     *                 El archivo se genera en una ubicación temporal y se elimina automáticamente
     *                 después de 5 minutos.
     *
     * @throws ReportGenerationException Si ocurre algún error durante la generación del reporte,
     *                                  como problemas de acceso a archivos, datos inválidos o
     *                                  errores en la creación del documento Excel.
     *                                  
     * @throws IllegalArgumentException Si entityName es null o vacío, o si requestData es null.
     */
    Resource generateReport(String entityName, Object requestData) throws ReportGenerationException;

}