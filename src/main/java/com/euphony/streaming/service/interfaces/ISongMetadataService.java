package com.euphony.streaming.service.interfaces;

import com.euphony.streaming.dto.response.SongMetadataResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Interfaz que define los métodos para analizar una canción.
 */
public interface ISongMetadataService {

    /**
     * Analiza la metadata de un archivo MP3 recibido.
     *
     * @param songFile archivo MP3 recibido.
     * @return DTO con los datos de la metadata.
     * @throws IOException si ocurre un error al manejar el archivo.
     */
    SongMetadataResponseDTO analyzeSongMetadata(MultipartFile songFile) throws IOException;

    /**
     * Extrae los metadatos de una canción.
     *
     * @param filePath Ruta del archivo de la canción.
     * @return Metadatos de la canción.
     */
    SongMetadataResponseDTO extractMetadata(String filePath);

    /**
     * Asigna los metadatos a una canción.
     *
     * @param songMetadataResponseDTO Metadatos de la canción.
     * @return Ruta del archivo de la canción con los metadatos asignados.
     */
    String assignMetadata(SongMetadataResponseDTO songMetadataResponseDTO);
}
