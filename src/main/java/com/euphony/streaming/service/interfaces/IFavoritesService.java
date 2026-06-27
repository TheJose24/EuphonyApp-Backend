package com.euphony.streaming.service.interfaces;

import com.euphony.streaming.dto.request.FavoriteAlbumRequestDTO;
import com.euphony.streaming.dto.request.FavoriteSongRequestDTO;
import com.euphony.streaming.dto.response.AlbumResponseDTO;
import com.euphony.streaming.dto.response.SongResponseDTO;

import java.util.List;
import java.util.UUID;

/**
 * Operaciones de favoritos por usuario: "me gusta" sobre canciones y álbumes favoritos.
 * Todas las mutaciones son idempotentes: marcar dos veces no rompe y quitar algo que no
 * estaba tampoco.
 */
public interface IFavoritesService {

    /**
     * Marca una canción como favorita ("me gusta") para el usuario. Idempotente.
     *
     * @param request DTO con userId (UUID) y songId (Long).
     */
    void likeSong(FavoriteSongRequestDTO request);

    /**
     * Quita el "me gusta" del usuario sobre una canción. Idempotente (no falla si no existía).
     *
     * @param request DTO con userId (UUID) y songId (Long).
     */
    void unlikeSong(FavoriteSongRequestDTO request);

    /**
     * Devuelve las canciones favoritas del usuario como {@link SongResponseDTO} enriquecido.
     *
     * @param userId Identificador del usuario (UUID).
     * @return Lista de canciones favoritas (vacía si no tiene).
     */
    List<SongResponseDTO> getFavoriteSongs(UUID userId);

    /**
     * Marca un álbum como favorito para el usuario. Idempotente.
     *
     * @param request DTO con userId (UUID) y albumId (Long).
     */
    void favoriteAlbum(FavoriteAlbumRequestDTO request);

    /**
     * Quita un álbum de los favoritos del usuario. Idempotente (no falla si no existía).
     *
     * @param request DTO con userId (UUID) y albumId (Long).
     */
    void unfavoriteAlbum(FavoriteAlbumRequestDTO request);

    /**
     * Devuelve los álbumes favoritos del usuario como {@link AlbumResponseDTO}.
     *
     * @param userId Identificador del usuario (UUID).
     * @return Lista de álbumes favoritos (vacía si no tiene).
     */
    List<AlbumResponseDTO> getFavoriteAlbums(UUID userId);
}
