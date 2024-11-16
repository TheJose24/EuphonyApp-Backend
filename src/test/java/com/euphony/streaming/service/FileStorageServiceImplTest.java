package com.euphony.streaming.service;

import com.euphony.streaming.exception.custom.storage.FileStorageException;
import com.euphony.streaming.service.implementation.FileStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileStorageServiceImplTest {

    private FileStorageServiceImpl fileStorageService;

    @Mock
    private MultipartFile multipartFile;

    @TempDir
    Path tempDir;

    private DataSize maxFileSize;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fileStorageService = new FileStorageServiceImpl();
        maxFileSize = DataSize.ofMegabytes(10);

        // Usar directorio temporal para almacenar archivos
        fileStorageService.setBaseUploadDir(tempDir.toString());
        fileStorageService.setMaxFileSize(maxFileSize);

        // Inicializar servicio
        fileStorageService.init();
    }

    @Test
    void testInit_CreatesDirectoriesSuccessfully() {
        // Verificar que se crearon los directorios para cada tipo de contenido
        FileStorageServiceImpl.ContentType[] contentTypes = FileStorageServiceImpl.ContentType.values();
        for (FileStorageServiceImpl.ContentType type : contentTypes) {
            Path typePath = tempDir.resolve(type.getDirectory());
            assertTrue(typePath.toFile().exists(),
                    "El directorio debe existir para el tipo de contenido: " + type.name());
        }
    }

    @Test
    void testStoreFile_SuccessfulStorage() throws IOException {
        // Arrange
        String fileName = "test-audio.mp3";
        String contentType = "AUDIO";
        byte[] fileContent = "test audio content".getBytes();

        when(multipartFile.getOriginalFilename()).thenReturn(fileName);
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getContentType()).thenReturn("audio/mpeg");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent));

        // Act
        String storedFilePath = fileStorageService.storeFile(multipartFile, contentType);

        // Assert
        assertNotNull(storedFilePath);
        assertTrue(storedFilePath.contains(tempDir.toString()));
        assertTrue(storedFilePath.endsWith(".mp3"));
    }

    @Test
    void testStoreFile_EmptyFileThrowsException() {
        // Arrange
        when(multipartFile.isEmpty()).thenReturn(true);
        String contentType = "AUDIO";

        // Act & Assert
        FileStorageException exception = assertThrows(
                FileStorageException.class,
                () -> fileStorageService.storeFile(multipartFile, contentType)
        );
        assertEquals("El archivo está vacío", exception.getMessage());
    }

    @Test
    void testStoreFile_InvalidFileNameThrowsException() {
        // Arrange
        String invalidFileName = "../test-audio.mp3";
        String contentType = "AUDIO";

        when(multipartFile.getOriginalFilename()).thenReturn(invalidFileName);
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getContentType()).thenReturn("audio/mpeg");

        // Act & Assert
        FileStorageException exception = assertThrows(
                FileStorageException.class,
                () -> fileStorageService.storeFile(multipartFile, contentType)
        );
        assertEquals("Nombre de archivo no válido: " + invalidFileName, exception.getMessage());
    }

    @Test
    void testStoreFile_InvalidContentTypeThrowsException() {
        // Arrange
        String contentType = "INVALID_TYPE";

        // Act & Assert
        FileStorageException exception = assertThrows(
                FileStorageException.class,
                () -> fileStorageService.storeFile(multipartFile, contentType)
        );
        assertEquals("Tipo de contenido no válido: " + contentType, exception.getMessage());
    }

    @Test
    void testStoreFile_ExceedingFileSizeThrowsException() {
        // Arrange
        String contentType = "AUDIO";
        long exceededSize = maxFileSize.toBytes() + 1;

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(exceededSize);
        when(multipartFile.getOriginalFilename()).thenReturn("large-file.mp3");
        when(multipartFile.getContentType()).thenReturn("audio/mpeg");

        // Act & Assert
        FileStorageException exception = assertThrows(
                FileStorageException.class,
                () -> fileStorageService.storeFile(multipartFile, contentType)
        );
        assertEquals(
                String.format("El archivo excede el tamaño máximo permitido de %s", maxFileSize),
                exception.getMessage()
        );
    }
}