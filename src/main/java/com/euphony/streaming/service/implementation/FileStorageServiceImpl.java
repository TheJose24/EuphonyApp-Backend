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

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
@Slf4j
public class FileStorageServiceImpl implements IFileStorageService {

    private static final class Constants {
        private static final DataSize DEFAULT_MAX_FILE_SIZE = DataSize.ofMegabytes(10);
        private static final String INVALID_PATH_SEQUENCE = "..";
        private static final String DOT = ".";
        private static final String FILE_SEPARATOR = "_";
        private static final int MAX_FILENAME_LENGTH = 15;
        private static final String HASH_ALGORITHM = "SHA-256";
    }

    @Getter
    @AllArgsConstructor
    public enum ContentType {
        IMAGE("image/", "images", Set.of("jpg", "jpeg", "png", "gif")),
        AUDIO("audio/", "audio", Set.of("mp3", "wav", "ogg")),
        PROFILE("image/", "profiles", Set.of("jpg", "jpeg", "png"));

        private final String mimeType;
        private final String directory;
        private final Set<String> allowedExtensions;
    }

    @Value("${app.upload-dir}")
    @Setter
    private String baseUploadDir;

    @Value("${app.max-file-size}")
    @Setter
    private DataSize maxFileSize = Constants.DEFAULT_MAX_FILE_SIZE;

    private final Map<ContentType, Path> uploadPaths = new EnumMap<>(ContentType.class);

    @PostConstruct
    public void init() {
        try {
            log.info("Inicializando directorios de almacenamiento de archivos...");
            setupUploadDirectories();
            log.info("Directorios de almacenamiento inicializados correctamente en: {}", baseUploadDir);
        } catch (Exception e) {
            String errorMessage = "Error crítico al inicializar el servicio de almacenamiento";
            log.error(errorMessage, e);
            throw new FileStorageException(errorMessage, e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void setupUploadDirectories() {
        Arrays.stream(ContentType.values())
                .forEach(contentType -> {
                    try {
                        Path path = createDirectory(contentType.getDirectory());
                        uploadPaths.put(contentType, path);
                        log.debug("Directorio creado para {}: {}", contentType, path);
                    } catch (IOException e) {
                        String errorMessage = String.format("No se pudo crear el directorio para %s", contentType);
                        log.error(errorMessage, e);
                        throw new FileStorageException(errorMessage, e, HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                });
    }

    private Path createDirectory(String dirName) throws IOException {
        Path path = Paths.get(baseUploadDir, dirName).toAbsolutePath().normalize();
        return Files.createDirectories(path);
    }

    @Override
    public String storeFile(@NotNull MultipartFile file, @NotBlank String contentType) {
        try {
            ContentType type = validateAndGetContentType(contentType);
            validateFile(file, type);

            String fileName = generateUniqueFileName(file);
            Path targetLocation = uploadPaths.get(type).resolve(fileName).normalize();

            log.info("Ruta de destino: {}", targetLocation);

            validateTargetLocation(targetLocation);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Archivo guardado exitosamente: {}", fileName);

            return buildPublicUrl(type, fileName);
        } catch (FileStorageException | IOException e) {
            throw new FileStorageException("Error inesperado al almacenar el archivo", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteFile(@NotBlank String filePath) {
        try {
            // Normalizar la ruta eliminando "/uploads/" si existe
            String normalizedPath = filePath.replace('\\', '/');
            if (normalizedPath.startsWith("/uploads/")) {
                normalizedPath = normalizedPath.substring("/uploads/".length());
            }

            // Construir la ruta completa
            Path fullPath = Paths.get(baseUploadDir, normalizedPath).normalize();
            log.info("Intentando eliminar archivo. Ruta base: {}", baseUploadDir);
            log.info("Ruta completa normalizada: {}", fullPath);

            // Validar que la ruta esté dentro del directorio base
            validateFilePath(fullPath);

            if (!Files.deleteIfExists(fullPath)) {
                log.warn("El archivo no existe: {}", filePath);
                throw new FileStorageException("Archivo no encontrado: " + filePath, HttpStatus.NOT_FOUND);
            }

            log.info("Archivo eliminado exitosamente: {}", filePath);
        } catch (FileStorageException | IOException e) {
            String errorMessage = String.format("Error al eliminar el archivo: %s", filePath);
            throw new FileStorageException(errorMessage , e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String updateExistingFile(@NotNull String filePath, @NotNull InputStream inputStream, @NotBlank String contentType) {
        try {
            ContentType type = validateAndGetContentType(contentType);

            String fileName = Path.of(filePath).getFileName().toString();
            Path targetLocation = uploadPaths.get(type).resolve(fileName).normalize();

            log.debug("Intentando actualizar archivo en la ruta: {}", targetLocation);

            // Validar que el archivo existe
            if (!Files.exists(targetLocation)) {
                log.warn("El archivo no existe en la ruta: {}", targetLocation);
                throw new FileStorageException("El archivo a actualizar no existe: " + targetLocation, HttpStatus.NOT_FOUND);
            }

            // Validar seguridad de la ubicación
            validateTargetLocation(targetLocation);

            // Sobrescribir el archivo
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Archivo actualizado exitosamente: {}", fileName);

            // Construir y devolver URL pública
            return buildPublicUrl(type, fileName);
        } catch (IOException | FileStorageException e) {
            String errorMessage = "Error de entrada/salida al actualizar el archivo";
            log.error(errorMessage, e);
            throw new FileStorageException(errorMessage, e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String buildPublicUrl(ContentType contentType, String fileName) {
        return String.format("/uploads/%s/%s", contentType.getDirectory(), fileName);
    }

    private ContentType validateAndGetContentType(String contentType) {
        try {
            return ContentType.valueOf(contentType.toUpperCase());
        } catch (IllegalArgumentException e) {
            String errorMessage = String.format("Tipo de contenido '%s' no válido. Valores permitidos: %s",
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
        validateFileExtension(file, contentType);
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

    private void validateFileExtension(MultipartFile file, ContentType contentType) {
        String extension = getFileExtension(file.getOriginalFilename());
        if (!contentType.getAllowedExtensions().contains(extension.toLowerCase())) {
            String errorMessage = String.format("Extensión de archivo no permitida: %s. Extensiones permitidas: %s",
                    extension, contentType.getAllowedExtensions());
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
        Path basePath = Paths.get(baseUploadDir).toAbsolutePath().normalize();
        if (!targetLocation.toAbsolutePath().normalize().startsWith(basePath)) {
            throw new FileStorageException("La ruta del archivo no es segura", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateFilePath(Path path) {
        try {
            Path basePath = Paths.get(baseUploadDir).toAbsolutePath().normalize();
            Path normalizedPath = path.toAbsolutePath().normalize();

            log.info("Validando ruta. Directorio base: {}", basePath);
            log.info("Ruta a validar: {}", normalizedPath);

            if (!normalizedPath.startsWith(basePath)) {
                throw new FileStorageException("Ruta de archivo no permitida: la ruta está fuera del directorio base",
                        HttpStatus.BAD_REQUEST);
            }

            // Verificar que el archivo existe
            if (!Files.exists(path)) {
                throw new FileStorageException("El archivo no existe: " + path,
                        HttpStatus.NOT_FOUND);
            }
        } catch (FileStorageException e) {
            throw new FileStorageException("Error al validar la ruta del archivo", e,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String generateUniqueFileName(MultipartFile file) {
        try {
            String originalName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            String extension = getFileExtension(originalName);
            String timestamp = String.valueOf(System.currentTimeMillis());
            String fileHash = generateFileHash(file);

            String truncatedName = originalName.substring(0,
                    Math.min(originalName.length(), Constants.MAX_FILENAME_LENGTH));

            return String.format("%s%s%s%s%s%s%s",
                    truncatedName,
                    Constants.FILE_SEPARATOR,
                    timestamp,
                    Constants.FILE_SEPARATOR,
                    fileHash,
                    Constants.DOT,
                    extension);

        } catch (Exception e) {
            throw new FileStorageException("Error al generar el nombre del archivo", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String generateFileHash(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(Constants.HASH_ALGORITHM);
        byte[] hash = digest.digest(file.getBytes());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash).substring(0, 8);
    }

    private String getFileExtension(String fileName) {
        return Optional.ofNullable(fileName)
                .filter(f -> f.contains(Constants.DOT))
                .map(f -> f.substring(f.lastIndexOf(Constants.DOT) + 1))
                .orElseThrow(() -> new FileStorageException("El archivo no tiene extensión", HttpStatus.BAD_REQUEST));
    }
}