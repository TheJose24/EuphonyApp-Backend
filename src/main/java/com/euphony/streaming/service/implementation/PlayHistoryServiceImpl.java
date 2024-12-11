package com.euphony.streaming.service.implementation;

import com.euphony.streaming.dto.response.PlayHistoryResponseDTO;
import com.euphony.streaming.entity.CancionEntity;
import com.euphony.streaming.entity.HistorialReproduccionEntity;
import com.euphony.streaming.entity.UsuarioEntity;
import com.euphony.streaming.exception.custom.playHistory.PlayHistoryException;
import com.euphony.streaming.exception.custom.song.SongNotFoundException;
import com.euphony.streaming.exception.custom.user.UserNotFoundException;
import com.euphony.streaming.repository.CancionRepository;
import com.euphony.streaming.repository.HistorialReproduccionRepository;
import com.euphony.streaming.repository.UsuarioRepository;
import com.euphony.streaming.service.interfaces.IPlayHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlayHistoryServiceImpl implements IPlayHistoryService {

    private final HistorialReproduccionRepository historialRepository;
    private final UsuarioRepository usuarioRepository;
    private final CancionRepository cancionRepository;

    @Override
    @Transactional
    public void recordPlay(UUID userId, Long songId) {
        try {
            log.info("Registrando reproducción: Usuario {} - Canción {}", userId, songId);

            UsuarioEntity user = usuarioRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado", HttpStatus.NOT_FOUND));

            CancionEntity song = cancionRepository.findById(songId)
                    .orElseThrow(() -> new SongNotFoundException("Canción no encontrada", HttpStatus.NOT_FOUND));

            HistorialReproduccionEntity historial = new HistorialReproduccionEntity();
            historial.setUsuario(user);
            historial.setCancion(song);
            historial.setFechaReproduccion(LocalDateTime.now());

            song.setNumeroReproducciones(song.getNumeroReproducciones() + 1);
            cancionRepository.save(song);

            historialRepository.save(historial);
            log.info("Reproducción registrada exitosamente");

        } catch (Exception e) {
            log.error("Error al registrar reproducción: {}", e.getMessage());
            throw new PlayHistoryException("Error al registrar reproducción", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayHistoryResponseDTO> getUserPlayHistory(UUID userId) {
        try {
            log.info("Obteniendo historial de reproducciones para usuario: {}", userId);

            if (!usuarioRepository.existsById(userId)) {
                throw new UserNotFoundException("Usuario no encontrado", HttpStatus.NOT_FOUND);
            }

            return historialRepository.findByUsuarioIdUsuarioOrderByFechaReproduccionDesc(userId)
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error al obtener historial: {}", e.getMessage());
            throw new PlayHistoryException("Error al obtener historial", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayHistoryResponseDTO> getRecentPlays(UUID userId, int limit) {
        try {
            log.info("Obteniendo {} reproducciones recientes para usuario: {}", limit, userId);

            Pageable pageable = PageRequest.of(0, limit, Sort.by("fechaReproduccion").descending());
            return historialRepository.findByUsuarioIdUsuario(userId, pageable)
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error al obtener reproducciones recientes: {}", e.getMessage());
            throw new PlayHistoryException("Error al obtener reproducciones recientes", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private PlayHistoryResponseDTO convertToDTO(HistorialReproduccionEntity historial) {
        return PlayHistoryResponseDTO.builder()
                .historyId(historial.getIdHistorial())
                .songId(historial.getCancion().getIdCancion())
                .songTitle(historial.getCancion().getTitulo())
                .artistName(historial.getCancion().getArtista().getNombre())
                .albumTitle(historial.getCancion().getAlbum() != null ?
                        historial.getCancion().getAlbum().getTitulo() : null)
                .playedAt(historial.getFechaReproduccion())
                .build();
    }
}
