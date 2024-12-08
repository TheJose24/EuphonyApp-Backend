package com.euphony.streaming.service.implementation;

import com.euphony.streaming.exception.custom.report.ReportGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportGeneratorServiceImpl {

    private static final int MARGIN_COLUMNS = 1;

    /**
     * Genera un reporte Excel para una entidad específica.
     *
     * @param entityName Nombre de la entidad (por ejemplo, "Users", "Playlists").
     * @param requestData Datos de la entidad en forma de lista.
     * @return Ruta del archivo generado.
     */
    public String generateReport(String entityName, Object requestData) {
        log.info("Generando reporte para la entidad: {}", entityName);
        log.info("Datos de entrada: {}", requestData);

        // Convertir a lista de datos
        List<?> dataList = convertToList(requestData);
        log.info("Lista de datos convertida: {}", dataList);

        // Verificar si la lista de datos está vacía
        if (dataList.isEmpty()) {
            throw new ReportGenerationException("La lista de datos está vacía. No se puede generar el reporte.");
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(entityName + "_Report");

        try {
            int currentRow = 0;

            // Agregar título al reporte
            currentRow = createTitleRow(sheet, entityName, currentRow);

            // Crear la tabla con los datos
            currentRow = createTable(sheet, dataList, currentRow, workbook);

            // Insertar la imagen
            insertImageToSheet(sheet, workbook);

            // Ajustar las columnas y filas
            adjustColumnAndRowSizes(sheet);

            // Guardar el reporte en un archivo
            String filePath = saveReportToFile(workbook, entityName);
            log.info("Reporte generado correctamente para la entidad '{}': {}", entityName, filePath);
            return filePath;

        } catch (IOException e) {
            log.error("Error al escribir el archivo para la entidad '{}': {}", entityName, e.getMessage(), e);
            throw new ReportGenerationException("No se pudo guardar el archivo para la entidad '" + entityName + "': " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Error inesperado al generar el reporte para la entidad '{}'", entityName, e);
            throw new ReportGenerationException("Error inesperado: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                log.error("Error al cerrar el Workbook para la entidad '{}': {}", entityName, e.getMessage());
            }
        }
    }

    /**
     * Convierte los datos proporcionados a una lista si no lo son ya.
     */
    private List<?> convertToList(Object requestData) {
        if (!(requestData instanceof List<?>)) {
            log.info("Convirtiendo requestData a lista: {}", requestData);
            return Collections.singletonList(requestData);
        }
        log.info("requestData ya es una lista: {}", requestData);
        return (List<?>) requestData;
    }
    /**
     * Crea la fila de título del reporte.
     */
    private int createTitleRow(Sheet sheet, String entityName, int currentRow) {
        Row titleRow = sheet.createRow(currentRow++);
        Cell titleCell = titleRow.createCell(MARGIN_COLUMNS);
        titleCell.setCellValue(entityName + " Report");
        titleCell.setCellStyle(createTitleCellStyle(sheet.getWorkbook()));
        sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, MARGIN_COLUMNS, MARGIN_COLUMNS + 5));
        return currentRow;
    }

    /**
     * Crea la tabla en el archivo Excel.
     */
    private int createTable(Sheet sheet, List<?> dataList, int startRow, Workbook workbook) {
        Object firstElement = dataList.get(0);
        Row headerRow = sheet.createRow(startRow++);
        CellStyle headerStyle = createHeaderCellStyle(workbook);
        CellStyle dataStyle = createDataCellStyle(workbook);

        if (firstElement instanceof Map<?, ?>) {
            return processMapData(sheet, dataList, headerRow, startRow, headerStyle, dataStyle);
        } else {
            try {
                return processObjectData(sheet, dataList, headerRow, startRow, headerStyle, dataStyle);
            } catch (IllegalAccessException e) {
                throw new ReportGenerationException("Error al acceder a los datos del objeto: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Procesa los datos en formato Map.
     */
    private int processMapData(Sheet sheet, List<?> dataList, Row headerRow, int startRow,
                               CellStyle headerStyle, CellStyle dataStyle) {
        Map<String, Object> firstMap = (Map<String, Object>) dataList.get(0);
        int columnIndex = MARGIN_COLUMNS;

        for (String key : firstMap.keySet()) {
            Cell headerCell = headerRow.createCell(columnIndex++);
            headerCell.setCellValue(key);
            headerCell.setCellStyle(headerStyle);
        }

        for (Object obj : dataList) {
            Map<String, Object> map = (Map<String, Object>) obj;
            Row dataRow = sheet.createRow(startRow++);
            columnIndex = MARGIN_COLUMNS;

            for (Object value : map.values()) {
                Cell dataCell = dataRow.createCell(columnIndex++);
                dataCell.setCellValue(value != null ? value.toString() : "N/A");
                dataCell.setCellStyle(dataStyle);
            }
        }

        return startRow;
    }

    /**
     * Procesa los datos en formato objeto (sin Map).
     */
    private int processObjectData(Sheet sheet, List<?> dataList, Row headerRow, int startRow,
                                  CellStyle headerStyle, CellStyle dataStyle) throws IllegalAccessException {
        Object firstElement = dataList.get(0);
        Field[] fields = firstElement.getClass().getDeclaredFields();
        int columnIndex = MARGIN_COLUMNS;

        // Crear encabezado con nombres de los campos
        for (Field field : fields) {
            field.setAccessible(true); // Permitir el acceso a campos privados
            Cell headerCell = headerRow.createCell(columnIndex++);
            headerCell.setCellValue(field.getName());
            headerCell.setCellStyle(headerStyle);
        }

        // Crear filas de datos
        for (Object obj : dataList) {
            Row dataRow = sheet.createRow(startRow++);
            columnIndex = MARGIN_COLUMNS;

            for (Field field : fields) {
                field.setAccessible(true); // Permitir el acceso a campos privados
                Cell dataCell = dataRow.createCell(columnIndex++);
                Object value = field.get(obj); // Obtener el valor del campo
                dataCell.setCellValue(value != null ? value.toString() : "N/A"); // Asignar el valor de la celda
                dataCell.setCellStyle(dataStyle);
            }
        }

        return startRow;
    }

    /**
     * Ajusta el tamaño de las columnas y filas para que las palabras estén alineadas horizontalmente.
     */
    private void adjustColumnAndRowSizes(Sheet sheet) {
        // Ajustar el ancho de las columnas a 30
        for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
            sheet.setColumnWidth(i, 30 * 256); // Establecer un ancho fijo para todas las columnas
        }

        // Ajustar la altura de las filas a 25
        for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                // Establecer la altura de las filas a 25 puntos
                row.setHeightInPoints(25);
            }
        }
    }

    private void insertImageToSheet(Sheet sheet, Workbook workbook) {
        try {
            String imagePath = "src/main/resources/static/img/logoEuphony.jpeg";
            File imageFile = new File(imagePath);

            if (!imageFile.exists()) {
                log.error("La imagen no se encuentra en la ruta especificada: {}", imagePath);
                throw new ReportGenerationException("Imagen no encontrada en la ruta especificada.");
            }

            InputStream imageInputStream = new FileInputStream(imageFile);
            byte[] imageBytes = imageInputStream.readAllBytes();
            int pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_JPEG);
            imageInputStream.close();

            CreationHelper helper = workbook.getCreationHelper();
            ClientAnchor anchor = helper.createClientAnchor();
            anchor.setCol1(0);  // Columna A
            anchor.setRow1(0);  // Fila 1
            anchor.setCol2(1);  // Columna B (una celda de ancho)
            anchor.setRow2(1);  // Fila 2 (una celda de alto)

            sheet.createDrawingPatriarch().createPicture(anchor, pictureIdx);

            log.info("Imagen insertada correctamente en la hoja Excel.");
        } catch (IOException e) {
            log.error("Error al insertar la imagen en la hoja de Excel: {}", e.getMessage(), e);
            throw new ReportGenerationException("Error al insertar la imagen en el reporte Excel: " + e.getMessage());
        }
    }
    /**
     * Guarda el archivo Excel generado en el sistema.
     */
    private String saveReportToFile(Workbook workbook, String entityName) throws IOException {
        String userHome = System.getProperty("user.home");
        String documentsPath = Paths.get(userHome, "Documents").toString();
        String fileName = entityName + "_Report_" + System.currentTimeMillis() + ".xlsx";
        File outputFile = new File(documentsPath, fileName);

        try (FileOutputStream fileOut = new FileOutputStream(outputFile)) {
            workbook.write(fileOut);
        }

        return outputFile.getAbsolutePath();
    }

    /**
     * Crea el estilo de celda para el título.
     */
    private CellStyle createTitleCellStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * Crea el estilo de celda para los encabezados.
     */
    private CellStyle createHeaderCellStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Crea el estilo de celda para los datos.
     */
    private CellStyle createDataCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);  // Centrado verticalmente
        style.setAlignment(HorizontalAlignment.LEFT);  // Alineación horizontal izquierda
        return style;
    }
}
