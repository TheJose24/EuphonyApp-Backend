package com.euphony.streaming.service.implementation;

import com.euphony.streaming.exception.custom.storage.FileStorageException;
import com.euphony.streaming.service.interfaces.IFileStorageService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
@Slf4j
public class FileStorageServiceImpl implements IFileStorageService {

    private static final class Constants {
        private static final DataSize MAX_FILE_SIZE = DataSize.ofMegabytes(10);
        private static final String INVALID_PATH_SEQUENCE = "..";
        private static final String DOT = ".";
    }

    @Getter
    @AllArgsConstructor
    public enum ContentType {
        IMAGE("image/", "images"),
        AUDIO("audio/", "audio"),
        PROFILE("image/", "profiles");

        private final String mimeType;
        private final String directory;

    }

    @Value("${app.upload-dir}")
    @Setter
    private String baseUploadDir;

    @Value("${app.max-file-size}")
    @Setter
    private DataSize maxFileSize = Constants.MAX_FILE_SIZE;

    private final Map<ContentType, Path> uploadPaths = new EnumMap<>(ContentType.class);

    @PostConstruct
    public void init() {
        try {
            log.info("Inicializando directorios de almacenamiento de archivos...");
            setupUploadDirectories();
            log.info("Directorios de almacenamiento inicializados correctamente");
        } catch (Exception e) {
            String errorMessage = "Error crítico al inicializar el servicio de almacenamiento";
            log.error(errorMessage, e);
            throw new FileStorageException(errorMessage, e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void setupUploadDirectories() {
        for (ContentType contentType : ContentType.values()) {
            try {
                Path path = createDirectory(contentType.getDirectory());
                uploadPaths.put(contentType, path);
                log.debug("Directorio creado para {}: {}", contentType, path);
            } catch (IOException e) {
                String errorMessage = String.format("No se pudo crear el directorio para %s", contentType);
                log.error(errorMessage, e);
                throw new FileStorageException(errorMessage, e, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    private Path createDirectory(String dirName) throws IOException {
        Path path = Paths.get(baseUploadDir, dirName).toAbsolutePath().normalize();
        return Files.createDirectories(path);
    }

    @Override
    public String storeFile(@NotNull MultipartFile file, @NotBlank String contentType) {
        ContentType type = validateAndGetContentType(contentType);
        validateFile(file, type);

        String fileName = generateUniqueFileName(file);
        Path targetLocation = uploadPaths.get(type).resolve(fileName).normalize();

        try {
            validateTargetLocation(targetLocation);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Archivo guardado exitosamente: {}", fileName);
            return targetLocation.toString();
        } catch (IOException e) {
            String errorMessage = String.format("Error al guardar el archivo: %s", fileName);
            log.error(errorMessage, e);
            throw new FileStorageException(errorMessage, e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteFile(@NotBlank String filePath) {
        try {
            Path path = Paths.get(filePath).normalize();
            validateFilePath(path);

            if (Files.deleteIfExists(path)) {
                log.info("Archivo eliminado exitosamente: {}", filePath);
            } else {
                log.warn("El archivo no existe: {}", filePath);
                throw new FileStorageException("Archivo no encontrado: " + filePath, HttpStatus.NOT_FOUND);
            }
        } catch (IOException e) {
            String errorMessage = String.format("Error al eliminar el archivo: %s", filePath);
            log.error(errorMessage, e);
            throw new FileStorageException(errorMessage, e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ContentType validateAndGetContentType(String contentType) {
        try {
            return ContentType.valueOf(contentType.toUpperCase());
        } catch (IllegalArgumentException e) {
            String errorMessage = String.format("El tipo de contenido '%s' no es válido. Los valores permitidos son: %s",
                    contentType, Arrays.toString(ContentType.values()));
            log.warn(errorMessage);
            throw new FileStorageException(errorMessage, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateFile(MultipartFile file, ContentType contentType) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("El archivo está vacío", HttpStatus.BAD_REQUEST);
        }

        validateFileSize(file);
        validateFileMimeType(file, contentType);
        validateFileName(file);
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > maxFileSize.toBytes()) {
            String errorMessage = String.format("El archivo excede el tamaño máximo permitido de %s", maxFileSize);
            log.warn(errorMessage);
            throw new FileStorageException(errorMessage, HttpStatus.PAYLOAD_TOO_LARGE);
        }
    }

    private void validateFileMimeType(MultipartFile file, ContentType contentType) {
        String fileMimeType = file.getContentType();
        if (fileMimeType == null || !fileMimeType.startsWith(contentType.getMimeType())) {
            String errorMessage = String.format("Tipo de archivo no permitido: %s", fileMimeType);
            log.warn(errorMessage);
            throw new FileStorageException(errorMessage, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
    }

    private void validateFileName(MultipartFile file) {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (fileName.contains(Constants.INVALID_PATH_SEQUENCE)) {
            String errorMessage = String.format("Nombre de archivo no válido: %s", fileName);
            log.warn(errorMessage);
            throw new FileStorageException(errorMessage, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateTargetLocation(Path targetLocation) {
        if (!targetLocation.getParent().equals(targetLocation.getParent().normalize())) {
            throw new FileStorageException("La ruta del archivo no es segura", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateFilePath(Path path) {
        if (!path.startsWith(Paths.get(baseUploadDir))) {
            throw new FileStorageException("Ruta de archivo no permitida", HttpStatus.BAD_REQUEST);
        }
    }

    private String generateUniqueFileName(MultipartFile file) {
        String originalName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = getFileExtension(originalName);
        String timestamp = String.valueOf(System.currentTimeMillis());
        // Fragmento del nombre original y un timestamp para mayor claridad
        return originalName.substring(0, Math.min(originalName.length(), 15)) + "_" + timestamp + Constants.DOT + extension;
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(Constants.DOT) + 1);
    }

}