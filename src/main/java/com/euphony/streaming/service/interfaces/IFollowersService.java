package com.euphony.streaming.service.interfaces;

import com.euphony.streaming.dto.request.FollowersArtistRequestDTO;
import com.euphony.streaming.dto.response.FollowersArtistResponseDTO;

import java.util.List;
import java.util.UUID;

/**
 * Interfaz que define las operaciones disponibles para el servicio de seguidores.
 */
public interface IFollowersService {

    /**
     * Permite a un usuario seguir a un artista.
     *
     * @param request DTO con la información del usuario y artista
     */
    void followArtist(FollowersArtistRequestDTO request);

    /**
     * Permite a un usuario dejar de seguir a un artista.
     *
     * @param request DTO con la información del usuario y artista
     */
    void unfollowArtist(FollowersArtistRequestDTO request);

    /**
     * Obtiene la lista de seguidores de un artista.
     *
     * @param artistId ID del artista
     * @return Lista de DTOs con la información de los seguidores
     */
    List<FollowersArtistResponseDTO> getFollowersByArtist(Long artistId);

    /**
     * Obtiene la lista de artistas que sigue un usuario.
     *
     * @param userId ID del usuario
     * @return Lista de DTOs con la información de los artistas seguidos
     */
    List<FollowersArtistResponseDTO> getFollowersByUser(UUID userId);
}