package com.euphony.streaming.service.interfaces;

import com.euphony.streaming.dto.request.SongRequestDTO;
import com.euphony.streaming.dto.response.SongMetadataResponseDTO;
import com.euphony.streaming.dto.response.SongResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * Interface que define los métodos que permiten realizar operaciones sobre las canciones.
 */
public interface ISongService {

    /**
     * Obtiene todas las canciones registradas en el sistema.
     *
     * @return Lista de {@link SongResponseDTO} con la información de todas las canciones.
     */
    List<SongResponseDTO> findAllSongs();

    /**
     * Obtiene información de una canción específica por su ID.
     *
     * @param songId El identificador único de la canción.
     * @return Un {@link SongResponseDTO} con los datos de la canción.
     */
    SongResponseDTO searchSongById(Long songId);

    /**
     * Crea una nueva canción en el sistema.
     *
     * @param song Un objeto {@link MultipartFile} con el archivo de la canción.
     * @param songRequestDTO Un objeto {@link SongRequestDTO} con los datos de la canción.
     */
    void createSong(MultipartFile song, SongRequestDTO songRequestDTO) throws IOException;

    /**
     * Elimina una canción del sistema.
     *
     * @param songId El identificador único de la canción a eliminar.
     */
    void deleteSong(Long songId);

    /**
     * Actualiza la información de una canción en el sistema.
     *
     * @param songId El identificador único de la canción a actualizar.
     * @param coverArtFile Un objeto {@link MultipartFile} con la imagen de la portada de la canción.
     * @param songRequestDTO Un objeto {@link SongRequestDTO} con los datos actualizados de la canción.
     */
    void updateSong(Long songId, MultipartFile coverArtFile, SongRequestDTO songRequestDTO) throws IOException;

    /**
     * Analiza un archivo de audio y extrae sus metadatos sin guardar el archivo.
     * @param songFile Archivo de audio a analizar.
     * @return Un {@link SongMetadataResponseDTO} con los metadatos de la canción.
     */
    SongMetadataResponseDTO analyzeSong(MultipartFile songFile) throws IOException;

    /**
     * Obtiene la ruta del archivo de una canción.
     * @param id Identificador de la canción.
     * @return Ruta del archivo de la canción.
     */
    Path getSongFilePath(Long id);

    /**
     * Obtiene las canciones de un álbum.
     *
     * @param albumId El identificador único del álbum.
     * @return Lista de {@link SongResponseDTO} de las canciones del álbum (vacía si el álbum no tiene canciones).
     */
    List<SongResponseDTO> findSongsByAlbum(Long albumId);

    /**
     * Obtiene las canciones de un artista.
     *
     * @param artistId El identificador único del artista.
     * @return Lista de {@link SongResponseDTO} de las canciones del artista (vacía si el artista no tiene canciones).
     */
    List<SongResponseDTO> findSongsByArtist(Long artistId);

    /**
     * Obtiene las canciones marcadas como favoritas ("me gusta") por un usuario, con el mismo
     * {@link SongResponseDTO} enriquecido (artista, álbum, géneros) que el resto de listados y
     * sin incurrir en el problema N+1.
     *
     * @param userId El identificador único del usuario (UUID).
     * @return Lista de {@link SongResponseDTO} (vacía si el usuario no tiene canciones favoritas).
     */
    List<SongResponseDTO> findFavoriteSongsByUser(UUID userId);

    /**
     * Obtiene las canciones de una playlist con el mismo {@link SongResponseDTO} enriquecido
     * (artista, álbum, géneros) que el resto de listados y sin incurrir en el problema N+1.
     * Las devuelve en el orden en que se añadieron a la playlist.
     *
     * @param playlistId El identificador único de la playlist.
     * @return Lista de {@link SongResponseDTO} (vacía si la playlist no tiene canciones).
     */
    List<SongResponseDTO> findSongsByPlaylist(Long playlistId);
}
