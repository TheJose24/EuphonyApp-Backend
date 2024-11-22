package com.euphony.streaming.service.implementation;

import com.euphony.streaming.dto.response.SongMetadataResponseDTO;
import com.euphony.streaming.exception.custom.metadata.InvalidMetadataException;
import com.euphony.streaming.exception.custom.metadata.MetadataProcessingException;
import com.euphony.streaming.service.interfaces.IFileStorageService;
import com.euphony.streaming.service.interfaces.ISongMetadataService;
import com.mpatric.mp3agic.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class SongMetadataServiceImpl implements ISongMetadataService {

    private final IFileStorageService fileStorageService;

    private static final String AUDIO_CONTENT_TYPE = "AUDIO";
    private static final String ERROR_METADATA_EXTRACTION = "Error al extraer la metadata del archivo: {}";
    private static final String ERROR_METADATA_ASSIGNMENT = "Error al asignar metadata al archivo: {}";
    private static final String ERROR_FILE_STORAGE = "Error al almacenar el archivo con metadata actualizada";
    private static final String ERROR_INVALID_FILE_PATH = "La ruta del archivo no puede estar vacía";
    private static final String ERROR_INVALID_METADATA = "La metadata no puede ser null";
    private static final String ERROR_INVALID_METADATA_PATH = "La ruta del archivo en la metadata no puede estar vacía";

    @Override
    public SongMetadataResponseDTO analyzeSongMetadata(MultipartFile songFile) throws IOException {
        validateFile(songFile);

        // Crear un archivo temporal para analizar
        Path tempFile = createTempFile();
        try {
            Files.copy(songFile.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            return extractMetadata(tempFile.toString());
        } catch (IOException e) {
            log.error("Error al analizar el archivo: {}", e.getMessage());
            throw new MetadataProcessingException("No se pudo analizar el archivo de audio", e, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Override
    public SongMetadataResponseDTO extractMetadata(String filePath) {
        validateFilePath(filePath);

        try {
            Path resolvedPath = resolveRelativePath(filePath);
            Mp3File mp3File = new Mp3File(resolvedPath.toString());
            return buildSongMetadata(mp3File, filePath);
        } catch (Exception e) {
            log.error(ERROR_METADATA_EXTRACTION, filePath, e);
            throw new MetadataProcessingException("No se pudo extraer la metadata del archivo", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String assignMetadata(SongMetadataResponseDTO songMetadataResponseDTO) {
        validateSongMetadata(songMetadataResponseDTO);

        try {
            Path inputFile = resolveRelativePath(songMetadataResponseDTO.getFilePath());
            Path tempOutputFile = createTempFile();

            try {
                return updateAndStoreMetadata(songMetadataResponseDTO, inputFile, tempOutputFile);
            } catch (InvalidDataException | UnsupportedTagException | NotSupportedException e) {
                log.error("Error al actualizar metadata en el archivo: {}", songMetadataResponseDTO.getFilePath(), e);
                throw new MetadataProcessingException("Error al actualizar metadata en el archivo", e, HttpStatus.INTERNAL_SERVER_ERROR);
            } finally {
                deleteTempFile(tempOutputFile);
            }
        } catch (IOException e) {
            log.error(ERROR_METADATA_ASSIGNMENT, songMetadataResponseDTO.getFilePath(), e);
            throw new MetadataProcessingException("Error al procesar el archivo de audio", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Valida que el archivo recibido sea un MP3 válido.
     *
     * @param songFile archivo recibido.
     */
    private static void validateFile(MultipartFile songFile) {
        if (songFile.isEmpty() || !Objects.requireNonNull(songFile.getOriginalFilename()).toLowerCase().endsWith(".mp3")) {
            throw new InvalidMetadataException("El archivo de canción está vacío o no es un archivo MP3 válido", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Resuelve una ruta relativa y verifica su existencia.
     *
     * @param relativePath ruta relativa.
     * @return Ruta absoluta resuelta.
     */
    private Path resolveRelativePath(String relativePath) {
        // Obtenemos la ruta base del directorio de trabajo actual
        Path basePath = Paths.get(System.getProperty("user.dir"));

        Path fullPath = basePath.resolve(relativePath.startsWith("/") ? relativePath.substring(1) : relativePath).normalize();

        // Comprobamos si el archivo o directorio existe
        if (!Files.exists(fullPath)) {
            throw new IllegalArgumentException("El archivo especificado no existe: " + fullPath);
        }

        return fullPath;
    }

    /**
     * Construye un DTO con la metadata extraída de un archivo MP3.
     *
     * @param mp3File archivo MP3.
     * @param filePath ruta del archivo.
     * @return DTO con los datos de la metadata.
     */
    private SongMetadataResponseDTO buildSongMetadata(Mp3File mp3File, String filePath) {
        SongMetadataResponseDTO.SongMetadataResponseDTOBuilder builder = SongMetadataResponseDTO.builder()
                .duration(String.valueOf(mp3File.getLengthInMilliseconds()))
                .filePath(filePath);

        if (mp3File.hasId3v2Tag()) {
            ID3v2 id3v2Tag = mp3File.getId3v2Tag();

            builder.title(id3v2Tag.getTitle())
                    .artist(id3v2Tag.getArtist())
                    .lyrics(id3v2Tag.getLyrics())
                    .releaseDate(id3v2Tag.getYear())
                    .album(id3v2Tag.getAlbum())
                    .genres(Collections.singleton(id3v2Tag.getGenreDescription()));
        }

        return builder.build();
    }

    private String updateAndStoreMetadata(SongMetadataResponseDTO songMetadataResponseDTO, Path inputFile, Path tempOutputFile) throws InvalidDataException, UnsupportedTagException, IOException, NotSupportedException {
        Mp3File mp3File = new Mp3File(inputFile.toFile());
        ID3v2 tag = mp3File.hasId3v2Tag() ? mp3File.getId3v2Tag() : new ID3v24Tag();

        applyMetadataToTag(tag, songMetadataResponseDTO);
        mp3File.setId3v2Tag(tag);
        mp3File.save(tempOutputFile.toString());

        return storeFileWithUpdatedMetadata(songMetadataResponseDTO.getFilePath(), tempOutputFile);
    }

    private void applyMetadataToTag(ID3v2 tag, SongMetadataResponseDTO metadata) {
        if (metadata.getTitle() != null) tag.setTitle(metadata.getTitle());
        if (metadata.getArtist() != null) tag.setArtist(metadata.getArtist());
        if (metadata.getAlbum() != null) tag.setAlbum(metadata.getAlbum());

        if (metadata.getAlbumCoverPath() != null) {
            try {
                byte[] imageBytes = loadImageFromProject(metadata.getAlbumCoverPath());
                String imageType = getImageType(metadata.getAlbumCoverPath());

                // Asignamos el tipo correcto según el formato de la imagen
                if ("png".equalsIgnoreCase(imageType)) {
                    tag.setAlbumImage(imageBytes, "image/png");
                } else if ("jpg".equalsIgnoreCase(imageType) || "jpeg".equalsIgnoreCase(imageType)) {
                    tag.setAlbumImage(imageBytes, "image/jpeg");
                } else {
                    log.warn("Tipo de imagen no soportado: {}", imageType);
                }
            } catch (IOException e) {
                log.warn("No se pudo cargar la imagen desde la ruta: {}", metadata.getAlbumCoverPath(), e);
            }
        }
    }

    private byte[] loadImageFromProject(String relativePath) throws IOException {
        Path imagePath = resolveRelativePath(relativePath);
        return Files.readAllBytes(imagePath);
    }

    private String getImageType(String imagePath) {
        return imagePath.substring(imagePath.lastIndexOf('.') + 1).toLowerCase();
    }

    private String storeFileWithUpdatedMetadata(String originalFilePath, Path tempOutputFile) throws IOException {
        try (InputStream inputStream = Files.newInputStream(tempOutputFile)) {
            return fileStorageService.updateExistingFile(originalFilePath, inputStream, AUDIO_CONTENT_TYPE);
        } catch (IOException e) {
            log.error(ERROR_FILE_STORAGE, e);
            throw new IOException(ERROR_FILE_STORAGE, e);
        }
    }

    /**
     * Crea un archivo temporal para procesamiento.
     *
     * @return Ruta del archivo temporal creado.
     * @throws IOException si ocurre un error al crear el archivo.
     */
    private Path createTempFile() throws IOException {
        return Files.createTempFile("metadata-", ".mp3");
    }

    /**
     * Elimina un archivo temporal si existe.
     *
     * @param tempFile Ruta del archivo temporal.
     */
    private void deleteTempFile(Path tempFile) {
        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            log.warn("No se pudo eliminar el archivo temporal: {}", tempFile, e);
        }
    }

    private void validateFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new InvalidMetadataException(ERROR_INVALID_FILE_PATH, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateSongMetadata(SongMetadataResponseDTO metadata) {
        if (metadata == null) {
            throw new InvalidMetadataException(ERROR_INVALID_METADATA, HttpStatus.BAD_REQUEST);
        }
        if (metadata.getFilePath() == null || metadata.getFilePath().trim().isEmpty()) {
            throw new InvalidMetadataException(ERROR_INVALID_METADATA_PATH, HttpStatus.BAD_REQUEST);
        }
    }
}
