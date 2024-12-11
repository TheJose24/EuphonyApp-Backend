package com.euphony.streaming.service.interfaces;

import com.euphony.streaming.dto.request.PlaylistRequestDTO;
import com.euphony.streaming.dto.response.PlaylistResponseDTO;
import com.euphony.streaming.dto.response.SongInPlaylistResponseDTO;

import java.util.List;

/**
 * Interfaz que define las operaciones de gestión de listas de reproducción.
 */
public interface IPlaylistService {

    /**
     * Obtiene todas las listas de reproducción registradas en el sistema.
     *
     * @return Lista de {@link PlaylistResponseDTO} con la información de todas las listas de reproducción.
     */
    List<PlaylistResponseDTO> findAllPlaylists();

    /**
     * Obtiene información de una lista de reproducción específica por su ID.
     *
     * @param id El identificador único de la lista de reproducción.
     * @return Un {@link PlaylistResponseDTO} con los datos de la lista de reproducción.
     */
    PlaylistResponseDTO findPlaylistById(Long id);

    /**
     * Crea una nueva lista de reproducción en el sistema.
     *
     * @param playlistRequestDTO Un objeto {@link PlaylistRequestDTO} con los datos de la nueva lista de reproducción.
     */
    void createPlaylist(PlaylistRequestDTO playlistRequestDTO);

    /**
     * Actualiza los datos de una lista de reproducción existente.
     *
     * @param id El identificador único de la lista de reproducción a actualizar.
     * @param playlistRequestDTO Un objeto {@link PlaylistRequestDTO} con los datos actualizados.
     */
    void updatePlaylist(Long id, PlaylistRequestDTO playlistRequestDTO);

    /**
     * Elimina una lista de reproducción del sistema.
     *
     * @param id El identificador único de la lista de reproducción a eliminar.
     */
    void deletePlaylist(Long id);


    /**
     * Agrega una canción a una lista de reproducción.
     *
     * @param playlistId El identificador único de la lista de reproducción.
     * @param songId El identificador único de la canción a agregar.
     */
    void addSongToPlaylist(Long playlistId, Long songId);

    /**
     * Obtiene las canciones de una lista de reproducción.
     *
     * @param playlistId El identificador único de la lista de reproducción.
     * @return Lista de {@link SongInPlaylistResponseDTO} con la información de las canciones de la lista de reproducción.
     */
    List<SongInPlaylistResponseDTO> getPlaylistSongs(Long playlistId);

    /**
     * Elimina una canción de una lista de reproducción.
     *
     * @param playlistId El identificador único de la lista de reproducción.
     * @param songId El identificador único de la canción a eliminar.
     */
    void removeSongFromPlaylist(Long playlistId, Long songId);
}
