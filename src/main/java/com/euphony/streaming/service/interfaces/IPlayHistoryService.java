package com.euphony.streaming.service.interfaces;

import com.euphony.streaming.dto.response.PlayHistoryResponseDTO;
import java.util.List;
import java.util.UUID;

/**
 * Interfaz que define los métodos para la gestión de historial de reproducción
 */
public interface IPlayHistoryService {
    /**
     * Registra la reproducción de una canción por un usuario
     * @param userId ID del usuario
     * @param songId ID de la canción
     */
    void recordPlay(UUID userId, Long songId);

    /**
     * Obtiene el historial de reproducción de un usuario
     * @param userId ID del usuario
     * @return Lista de reproducciones
     */
    List<PlayHistoryResponseDTO> getUserPlayHistory(UUID userId);

    /**
     * Obtiene las reproducciones recientes de un usuario
     * @param userId ID del usuario
     * @param limit Límite de reproducciones
     * @return Lista de reproducciones recientes
     */
    List<PlayHistoryResponseDTO> getRecentPlays(UUID userId, int limit);
}
