package com.euphony.streaming.service.implementation;

import com.euphony.streaming.exception.custom.report.ReportGenerationException;
import com.euphony.streaming.service.interfaces.IReportGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportGeneratorServiceImpl implements IReportGeneratorService {

    private static final int MARGIN_COLUMNS = 1;
    private static final int DEFAULT_COLUMN_WIDTH = 30;
    private static final int DEFAULT_ROW_HEIGHT = 25;
    private static final String IMAGE_PATH = "src/main/resources/static/img/logoEuphony.jpeg";
    private static final String FILE_EXTENSION = ".xlsx";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // Color constants
    private static final byte[] BRAND_COLOR = new byte[]{63, 81, (byte) 181}; // Material Design Indigo
    private static final byte[] SECONDARY_COLOR = new byte[]{(byte) 144, (byte) 164, (byte) 174}; // Material Design Blue Grey
    private static final byte[] HEADER_BG_COLOR = new byte[]{(byte) 232,(byte) 234,(byte) 246}; // Light Indigo
    private static final byte[] ALTERNATE_ROW_COLOR = new byte[]{(byte) 248,(byte) 249,(byte) 250}; // Light Grey

    @Value("${app.reports.temp-dir:temp/reports}")
    private String tempReportsDir;

    @Override
    public Resource generateReport(String entityName, Object requestData) {
        Objects.requireNonNull(entityName, "Entity name cannot be null");
        Objects.requireNonNull(requestData, "Request data cannot be null");

        log.info("Generating report for entity: {}", entityName);
        log.debug("Input data: {}", requestData);

        List<?> dataList = convertToList(requestData);
        validateData(dataList);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sanitizeSheetName(entityName + "_Report"));
            int currentRow = 0;

            currentRow = createTitleRow(sheet, entityName, currentRow);
            currentRow = createTable(sheet, dataList, currentRow, workbook);

            insertImageToSheet(sheet, workbook);
            adjustColumnAndRowSizes(sheet);

            return saveReportToFile(workbook, entityName);
        } catch (IOException e) {
            throw new ReportGenerationException("Failed to generate report for entity: " + entityName, e);
        }
    }

    private void validateData(List<?> dataList) {
        if (dataList.isEmpty()) {
            throw new ReportGenerationException("Cannot generate report: Empty data list");
        }
    }

    private List<?> convertToList(Object requestData) {
        if (!(requestData instanceof List<?>)) {
            log.info("Convirtiendo requestData a lista: {}", requestData);
            return Collections.singletonList(requestData);
        }
        log.info("requestData ya es una lista: {}", requestData);
        return (List<?>) requestData;
    }

    private String sanitizeSheetName(String name) {
        return name.replaceAll("[\\\\/:?*\\[\\]]", "_");
    }

    private int createTitleRow(Sheet sheet, String entityName, int currentRow) {
        Row titleRow = sheet.createRow(currentRow++);
        titleRow.setHeightInPoints(50); // Taller title row

        Cell titleCell = titleRow.createCell(MARGIN_COLUMNS);
        titleCell.setCellValue("Euphony Music Streaming");

        Row subtitleRow = sheet.createRow(currentRow++);
        Cell subtitleCell = subtitleRow.createCell(MARGIN_COLUMNS);
        subtitleCell.setCellValue(entityName + " Report");

        Row dateRow = sheet.createRow(currentRow++);
        Cell dateCell = dateRow.createCell(MARGIN_COLUMNS);
        dateCell.setCellValue("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm")));

        // Merge cells for all rows
        int lastColumn = MARGIN_COLUMNS + 6;
        sheet.addMergedRegion(new CellRangeAddress(currentRow - 3, currentRow - 3, MARGIN_COLUMNS, lastColumn));
        sheet.addMergedRegion(new CellRangeAddress(currentRow - 2, currentRow - 2, MARGIN_COLUMNS, lastColumn));
        sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, MARGIN_COLUMNS, lastColumn));

        // Apply styles
        titleCell.setCellStyle(createTitleCellStyle(sheet.getWorkbook()));
        subtitleCell.setCellStyle(createSubtitleCellStyle(sheet.getWorkbook()));
        dateCell.setCellStyle(createDateCellStyle(sheet.getWorkbook()));

        // Add spacing after the header
        sheet.createRow(currentRow++);

        return currentRow;
    }

    private CellStyle createTitleCellStyle(Workbook workbook) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 24);
        font.setFontName("Arial");
        font.setColor(new XSSFColor(BRAND_COLOR, null));

        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createSubtitleCellStyle(Workbook workbook) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setFontName("Arial");
        font.setColor(new XSSFColor(SECONDARY_COLOR, null));

        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDateCellStyle(Workbook workbook) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Arial");
        font.setColor(new XSSFColor(SECONDARY_COLOR, null));

        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderCellStyle(Workbook workbook) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setFontName("Arial");
        font.setColor(new XSSFColor(BRAND_COLOR, null));

        style.setFont(font);
        style.setFillForegroundColor(new XSSFColor(HEADER_BG_COLOR, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);

        // Add subtle borders
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBottomBorderColor(new XSSFColor(BRAND_COLOR, null));
        style.setTopBorderColor(new XSSFColor(BRAND_COLOR, null));

        return style;
    }

    private CellStyle createDataCellStyle(Workbook workbook) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Arial");

        style.setFont(font);
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.LEFT);

        // Add subtle borders only on bottom
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(new XSSFColor(SECONDARY_COLOR, null));

        return style;
    }

    private CellStyle createAlternateRowStyle(Workbook workbook) {
        XSSFCellStyle style = (XSSFCellStyle) createDataCellStyle(workbook);
        style.setFillForegroundColor(new XSSFColor(ALTERNATE_ROW_COLOR, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private int createTable(Sheet sheet, List<?> dataList, int startRow, Workbook workbook) {
        Object firstElement = dataList.get(0);
        Row headerRow = sheet.createRow(startRow++);
        CellStyle headerStyle = createHeaderCellStyle(workbook);
        CellStyle dataStyle = createDataCellStyle(workbook);

        return (firstElement instanceof Map)
                ? processMapData(sheet, dataList, headerRow, startRow, headerStyle, dataStyle)
                : processObjectData(sheet, dataList, headerRow, startRow, headerStyle, dataStyle);
    }

    private int processObjectData(Sheet sheet, List<?> dataList, Row headerRow, int startRow,
                                  CellStyle headerStyle, CellStyle dataStyle) {
        Field[] fields = dataList.get(0).getClass().getDeclaredFields();
        int columnIndex = MARGIN_COLUMNS;

        // Create headers
        for (Field field : fields) {
            field.setAccessible(true);
            createCell(headerRow, columnIndex++, formatHeaderName(field.getName()), headerStyle);
        }

        // Create data rows with alternating styles
        CellStyle alternateStyle = createAlternateRowStyle(sheet.getWorkbook());
        int rowNum = 0;

        for (Object obj : dataList) {
            Row dataRow = sheet.createRow(startRow++);
            columnIndex = MARGIN_COLUMNS;
            CellStyle currentStyle = (rowNum % 2 == 0) ? dataStyle : alternateStyle;
            rowNum++;

            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    createCell(dataRow, columnIndex++, formatCellValue(value), currentStyle);
                } catch (IllegalAccessException e) {
                    log.error("Failed to access field: {}", field.getName(), e);
                    createCell(dataRow, columnIndex++, "N/A", currentStyle);
                }
            }
        }

        return startRow;
    }

    private int processMapData(Sheet sheet, List<?> dataList, Row headerRow, int startRow,
                               CellStyle headerStyle, CellStyle dataStyle) {
        Map<?, ?> firstMap = (Map<?, ?>) dataList.get(0);
        int columnIndex = MARGIN_COLUMNS;

        for (Object key : firstMap.keySet()) {
            createCell(headerRow, columnIndex++, key.toString(), headerStyle);
        }

        for (Object obj : dataList) {
            Map<?, ?> map = (Map<?, ?>) obj;
            Row dataRow = sheet.createRow(startRow++);
            columnIndex = MARGIN_COLUMNS;

            for (Object value : map.values()) {
                createCell(dataRow, columnIndex++, formatCellValue(value), dataStyle);
            }
        }

        return startRow;
    }

    private String formatHeaderName(String fieldName) {
        // Convert camelCase to Title Case with Spaces
        String[] words = fieldName.split("(?=[A-Z])");
        return Arrays.stream(words)
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private String formatCellValue(Object value) {
        return value != null ? value.toString() : "N/A";
    }

    private void adjustColumnAndRowSizes(Sheet sheet) {
        for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
            sheet.setColumnWidth(i, DEFAULT_COLUMN_WIDTH * 256);
        }

        for (Row row : sheet) {
            row.setHeightInPoints(DEFAULT_ROW_HEIGHT);
        }
    }

    private void insertImageToSheet(Sheet sheet, Workbook workbook) {
        try (InputStream imageStream = new FileInputStream(IMAGE_PATH)) {
            byte[] imageBytes = imageStream.readAllBytes();
            int pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_JPEG);

            CreationHelper helper = workbook.getCreationHelper();
            ClientAnchor anchor = helper.createClientAnchor();
            anchor.setCol1(0);
            anchor.setRow1(0);
            anchor.setCol2(1);
            anchor.setRow2(1);

            sheet.createDrawingPatriarch().createPicture(anchor, pictureIdx);
        } catch (IOException e) {
            log.error("Failed to insert image into sheet", e);
        }
    }

    private Resource saveReportToFile(Workbook workbook, String entityName) throws IOException {
        Path reportDir = Paths.get(tempReportsDir);
        Files.createDirectories(reportDir);

        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String fileName = String.format("%s_Report_%s%s", entityName, timestamp, FILE_EXTENSION);
        Path filePath = reportDir.resolve(fileName);

        log.info("Saving report file to: {}", filePath);

        try (OutputStream fileOut = Files.newOutputStream(filePath)) {
            workbook.write(fileOut);
        }

        scheduleFileDeletion(filePath);

        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists() || resource.isReadable()) {
            return resource;
        } else {
            throw new ReportGenerationException("Failed to create report file");
        }
    }

    private void scheduleFileDeletion(Path filePath) {
        new Thread(() -> {
            try {
                // 5 minutos
                Thread.sleep(5 * 60 * 1000);
                Files.deleteIfExists(filePath);
                log.info("Temporary report file deleted: {}", filePath);
            } catch (Exception e) {
                log.error("Error deleting temporary report file: {}", filePath, e);
            }
        }).start();
    }

}