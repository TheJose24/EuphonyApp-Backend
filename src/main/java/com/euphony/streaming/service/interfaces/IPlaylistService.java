package com.euphony.streaming.service.interfaces;

import com.euphony.streaming.dto.request.PlaylistRequestDTO;
import com.euphony.streaming.dto.response.PlaylistResponseDTO;
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
}
