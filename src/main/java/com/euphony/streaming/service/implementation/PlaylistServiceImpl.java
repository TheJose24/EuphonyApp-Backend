package com.euphony.streaming.service.implementation;

import com.euphony.streaming.dto.request.PlaylistRequestDTO;
import com.euphony.streaming.dto.response.PlaylistResponseDTO;
import com.euphony.streaming.dto.response.SongResponseDTO;
import com.euphony.streaming.entity.*;
import com.euphony.streaming.exception.custom.playlist.PlaylistCreationException;
import com.euphony.streaming.exception.custom.playlist.PlaylistDeletionException;
import com.euphony.streaming.exception.custom.playlist.PlaylistNotFoundException;
import com.euphony.streaming.exception.custom.playlist.PlaylistUpdateException;
import com.euphony.streaming.exception.custom.song.SongNotFoundException;
import com.euphony.streaming.exception.custom.user.UserNotFoundException;
import com.euphony.streaming.repository.CancionRepository;
import com.euphony.streaming.repository.PlaylistCancionRepository;
import com.euphony.streaming.repository.PlaylistRepository;
import com.euphony.streaming.repository.UsuarioRepository;
import com.euphony.streaming.service.interfaces.IPlaylistService;
import com.euphony.streaming.service.interfaces.ISongService;
import com.euphony.streaming.util.PlaylistSongCountProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlaylistServiceImpl implements IPlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistCancionRepository playlistCancionRepository;
    private final UsuarioRepository usuarioRepository;
    private final CancionRepository songRepository;
    private final ISongService songService;

    @Override
    public List<PlaylistResponseDTO> findAllPlaylists() {
        try {
            log.info("Iniciando búsqueda de todas las listas de reproducción");
            List<PlaylistEntity> entities = playlistRepository.findAll();
            Map<Long, Integer> counts = songCountsFor(entities);
            List<PlaylistResponseDTO> playlists = entities.stream()
                    .map(entity -> convertToDTO(entity, counts.getOrDefault(entity.getIdPlaylist(), 0)))
                    .collect(Collectors.toList());
            log.info("Búsqueda completada. Se encontraron {} listas de reproducción", playlists.size());
            return playlists;
        } catch (DataAccessException e) {
            log.error("Error al acceder a la base de datos durante la búsqueda de listas de reproducción: {}", e.getMessage());
            throw new PlaylistCreationException("Error al obtener las listas de reproducción", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Error inesperado al buscar listas de reproducción: {}", e.getMessage());
            throw new PlaylistCreationException("Error inesperado al buscar listas de reproducción", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public PlaylistResponseDTO findPlaylistById(Long id) {
        try {
            log.info("Iniciando búsqueda de lista de reproducción con ID: {}", id);
            return playlistRepository.findById(id)
                    .map(playlist -> {
                        log.info("Lista de reproducción encontrada: {} (ID: {})", playlist.getNombre(), playlist.getIdPlaylist());
                        return convertToDTO(playlist, (int) playlistCancionRepository.countByPlaylistIdPlaylist(playlist.getIdPlaylist()));
                    })
                    .orElseThrow(() -> {
                        log.error("No se encontró la lista de reproducción con ID: {}", id);
                        return new PlaylistNotFoundException("No se encontró la lista de reproducción con ID: " + id);
                    });
        } catch (PlaylistNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al buscar lista de reproducción con ID {}: {}", id, e.getMessage());
            throw new PlaylistCreationException("Error al buscar la lista de reproducción", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistResponseDTO> findPlaylistsByUserId(UUID userId) {
        try {
            log.info("Buscando playlists del usuario ID: {}", userId);

            // Validar que el usuario exista
            if (!usuarioRepository.existsById(userId)) {
                log.error("No se encontró el usuario con ID: {}", userId);
                throw new UserNotFoundException(
                        "No se encontró el usuario especificado",
                        HttpStatus.NOT_FOUND
                );
            }

            // Obtener playlists del usuario
            List<PlaylistEntity> playlists = playlistRepository.findByUsuarioIdUsuario(userId);

            // Convertir a DTOs (conteo de canciones batcheado para evitar N+1)
            Map<Long, Integer> counts = songCountsFor(playlists);
            List<PlaylistResponseDTO> playlistDTOs = playlists.stream()
                    .map(playlist -> convertToDTO(playlist, counts.getOrDefault(playlist.getIdPlaylist(), 0)))
                    .collect(Collectors.toList());

            log.info("Se encontraron {} playlists para el usuario {}", playlistDTOs.size(), userId);
            return playlistDTOs;

        } catch (UserNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("Error de base de datos al buscar playlists del usuario {}: {}", userId, e.getMessage());
            throw new PlaylistNotFoundException(
                    "Error al obtener las playlists del usuario",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    @Override
    @Transactional
    public void createPlaylist(PlaylistRequestDTO playlistRequestDTO) {
        try {
            log.info("Iniciando creación de lista de reproducción para usuario UUID: {}", playlistRequestDTO.getUserId());

            // Validar si el nombre está vacío
            if (playlistRequestDTO.getName() == null || playlistRequestDTO.getName().trim().isEmpty()) {
                log.error("Error de validación: El nombre de la playlist no puede estar vacío o contener solo espacios");
                throw new PlaylistCreationException("El nombre de la playlist es obligatorio y no puede estar vacío o contener solo espacios en blanco", HttpStatus.BAD_REQUEST);
            }

            // Validar existencia de usuario antes de crear la playlist
            UsuarioEntity usuario = usuarioRepository.findById(playlistRequestDTO.getUserId())
                    .orElseThrow(() -> {
                        log.error("Error de usuario: No se encontró ningún usuario con el ID: {}. Verifique que el ID sea correcto", playlistRequestDTO.getUserId());
                        return new UserNotFoundException("No se encontró ningún usuario con el ID proporcionado. Por favor, verifique que el usuario exista", HttpStatus.NOT_FOUND);
                    });

            // Validar si ya existe una playlist similar
            boolean playlistDuplicated = playlistRepository.findAll().stream()
                    .anyMatch(p -> p.getNombre().equalsIgnoreCase(playlistRequestDTO.getName())
                            && p.getUsuario().getIdUsuario().equals(playlistRequestDTO.getUserId()));

            if (playlistDuplicated) {
                log.error("Error de duplicidad: El usuario con ID {} ya tiene una playlist con el nombre '{}'", playlistRequestDTO.getUserId(), playlistRequestDTO.getName());
                throw new PlaylistCreationException("No se puede crear la playlist porque ya existe una con el mismo nombre para este usuario. Por favor, elija un nombre diferente",
                        HttpStatus.CONFLICT);
            }

            PlaylistEntity playlist = new PlaylistEntity();
            playlist.setNombre(playlistRequestDTO.getName());
            playlist.setDescripcion(playlistRequestDTO.getDescription());
            playlist.setIsPublic(playlistRequestDTO.getIsPublic());
            playlist.setImgPortada(playlistRequestDTO.getCoverImage());
            playlist.setUsuario(usuario);
            playlist.setFechaCreacion(LocalDate.now());

            try {
                playlistRepository.save(playlist);
                log.info("Lista de reproducción '{}' creada exitosamente para el usuario ID: {}", playlist.getNombre(), playlist.getUsuario().getIdUsuario());
            } catch (DataIntegrityViolationException e) {
                log.error("Error de integridad en la base de datos al crear la playlist: {}. Detalles: {}", playlist.getNombre(), e.getMessage());
                throw new PlaylistCreationException("No se pudo crear la playlist debido a un error de integridad en la base de datos. Por favor, verifique que todos los campos obligatorios estén correctamente proporcionados", HttpStatus.CONFLICT);
            }

        } catch (UserNotFoundException e) {
            throw e;
        } catch (PlaylistCreationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado durante la creación de la playlist. Detalles: {}. Causa: {}", e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "No disponible");
            throw new PlaylistCreationException("Ocurrió un error inesperado durante la creación de la playlist. Por favor, inténtelo nuevamente o contacte al administrador del sistema", HttpStatus.BAD_REQUEST);
        }
    }
    @Override
    @Transactional
    public void updatePlaylist(Long id, PlaylistRequestDTO playlistRequestDTO) {
        try {
            log.info("Iniciando validación de campos para la playlist ID: {}", id);

            // Validación del ID
            if (id <= 0) {
                throw new PlaylistUpdateException(
                        "El ID de la playlist no puede ser menor o igual a cero. ID proporcionado: " + id,
                        HttpStatus.BAD_REQUEST
                );
            }

            // Validación del nombre
            if (playlistRequestDTO.getName() == null) {
                throw new PlaylistUpdateException(
                        "El nombre de la playlist es obligatorio y no puede ser nulo",
                        HttpStatus.BAD_REQUEST
                );
            }
            if (playlistRequestDTO.getName().trim().isEmpty()) {
                throw new PlaylistUpdateException(
                        "El nombre de la playlist no puede estar vacío o contener solo espacios",
                        HttpStatus.BAD_REQUEST
                );
            }
            if (playlistRequestDTO.getName().length() > 100) {
                throw new PlaylistUpdateException(
                        "El nombre de la playlist no puede exceder los 100 caracteres. Longitud actual: " +
                                playlistRequestDTO.getName().length(),
                        HttpStatus.BAD_REQUEST
                );
            }

            // Validación de la descripción
            if (playlistRequestDTO.getDescription() == null) {
                throw new PlaylistUpdateException(
                        "La descripción de la playlist es obligatoria y no puede ser nula",
                        HttpStatus.BAD_REQUEST
                );
            }
            if (playlistRequestDTO.getDescription().trim().isEmpty()) {
                throw new PlaylistUpdateException(
                        "La descripción de la playlist no puede estar vacía o contener solo espacios",
                        HttpStatus.BAD_REQUEST
                );
            }
            if (playlistRequestDTO.getDescription().length() > 500) {
                throw new PlaylistUpdateException(
                        "La descripción de la playlist no puede exceder los 500 caracteres. Longitud actual: " +
                                playlistRequestDTO.getDescription().length(),
                        HttpStatus.BAD_REQUEST
                );
            }

            // Validación del estado público
            if (playlistRequestDTO.getIsPublic() == null) {
                throw new PlaylistUpdateException(
                        "El estado público/privado de la playlist es obligatorio y no puede ser nulo",
                        HttpStatus.BAD_REQUEST
                );
            }

            // Validación de la imagen de portada
            if (playlistRequestDTO.getCoverImage() == null) {
                throw new PlaylistUpdateException(
                        "La URL de la imagen de portada es obligatoria y no puede ser nula",
                        HttpStatus.BAD_REQUEST
                );
            }
            if (playlistRequestDTO.getCoverImage().trim().isEmpty()) {
                throw new PlaylistUpdateException(
                        "La URL de la imagen de portada no puede estar vacía o contener solo espacios",
                        HttpStatus.BAD_REQUEST
                );
            }
            if (!playlistRequestDTO.getCoverImage().startsWith("http://") &&
                    !playlistRequestDTO.getCoverImage().startsWith("https://")) {
                throw new PlaylistUpdateException(
                        "La URL de la imagen de portada debe comenzar con 'http://' o 'https://'",
                        HttpStatus.BAD_REQUEST
                );
            }

            // Validación de existencia de la playlist
            PlaylistEntity playlist = playlistRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("No se encontró la playlist con ID: {}", id);
                        return new PlaylistNotFoundException(
                                "No se encontró la playlist con ID: " + id + ". Verifica que el ID sea correcto."
                        );
                    });

            // Actualización de los campos
            playlist.setNombre(playlistRequestDTO.getName());
            playlist.setDescripcion(playlistRequestDTO.getDescription());
            playlist.setIsPublic(playlistRequestDTO.getIsPublic());
            playlist.setImgPortada(playlistRequestDTO.getCoverImage());

            playlist = playlistRepository.save(playlist);
            log.info("Playlist actualizada exitosamente - ID: {}, Nombre: {}",
                    playlist.getIdPlaylist(), playlist.getNombre());

        } catch (DataAccessException e) {
            log.error("Error de base de datos al actualizar la playlist {}: {}", id, e.getMessage());
            throw new PlaylistUpdateException(
                    "Error al acceder a la base de datos. Por favor, inténtalo más tarde.",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        } catch (PlaylistNotFoundException | PlaylistUpdateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al actualizar la playlist {}: {}", id, e.getMessage());
            throw new PlaylistUpdateException(
                    "Ha ocurrido un error inesperado. Por favor, contacta al administrador.",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    @Override
    @Transactional
    public void deletePlaylist(Long id) {
        try {
            // Validar ID no nulo
            if (id == null) {
                log.error("Error de validación: Se intentó eliminar una playlist con ID nulo");
                throw new PlaylistDeletionException("El ID de la playlist es obligatorio y no puede ser nulo", HttpStatus.BAD_REQUEST);
            }

            // Validar ID positivo
            if (id <= 0) {
                log.error("Error de validación: ID no válido: {}", id);
                throw new PlaylistDeletionException("El ID debe ser un número positivo mayor a cero. ID proporcionado: " + id, HttpStatus.BAD_REQUEST);
            }

            log.info("Iniciando eliminación de la playlist con ID: {}", id);

            // Buscar la playlist
            PlaylistEntity playlist = playlistRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("No se encontró la playlist con ID: {}", id);
                        return new PlaylistNotFoundException("No se encontró ninguna playlist con el ID: " + id, HttpStatus.NOT_FOUND);
                    });

            // Eliminar la playlist
            playlistRepository.deleteById(id);
            log.info("La playlist con ID: {} fue eliminada exitosamente", id);

        } catch (PlaylistNotFoundException e) {
            // Manejo de error 404
            throw e;
        } catch (DataAccessException e) {
            // Manejo de error 500
            log.error("Error de base de datos al eliminar la playlist {}: {}", id, e.getMessage());
            throw new PlaylistDeletionException("Error en el servidor al intentar eliminar la playlist. Intente nuevamente más tarde.", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (PlaylistDeletionException e) {
            // Error personalizado
            throw e;
        } catch (Exception e) {
            // Manejo de error inesperado
            log.error("Error inesperado al eliminar la playlist {}: {}", id, e.getMessage());
            throw new PlaylistDeletionException("Se produjo un error inesperado. Intente nuevamente más tarde.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<SongResponseDTO> getPlaylistSongs(Long playlistId) {
        log.info("Buscando canciones de la playlist ID: {}", playlistId);

        // Validar existencia de la playlist (404 si no existe).
        if (!playlistRepository.existsById(playlistId)) {
            log.error("No se encontró la playlist con ID: {}", playlistId);
            throw new PlaylistNotFoundException("No se encontró la playlist con ID: " + playlistId, HttpStatus.NOT_FOUND);
        }

        // Reutiliza el mismo SongResponseDTO enriquecido (artista, álbum, géneros) y el patrón sin
        // N+1 del módulo de canciones; las devuelve en el orden en que se añadieron.
        List<SongResponseDTO> songs = songService.findSongsByPlaylist(playlistId);
        log.info("Se encontraron {} canciones en la playlist {}", songs.size(), playlistId);
        return songs;
    }


    @Override
    @Transactional
    public void addSongToPlaylist(Long playlistId, Long songId) {
        log.info("Iniciando proceso de agregar canción {} a playlist {}", songId, playlistId);

        // Validar playlist (404).
        PlaylistEntity playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> {
                    log.error("No se encontró la playlist con ID: {}", playlistId);
                    return new PlaylistNotFoundException("No se encontró la playlist con ID: " + playlistId, HttpStatus.NOT_FOUND);
                });

        // Validar canción (404).
        CancionEntity song = songRepository.findById(songId)
                .orElseThrow(() -> {
                    log.error("No se encontró la canción con ID: {}", songId);
                    return new SongNotFoundException("No se encontró la canción con ID: " + songId, HttpStatus.NOT_FOUND);
                });

        // Idempotente: si la canción ya está en la playlist, no se inserta de nuevo.
        if (playlistCancionRepository.existsByPlaylistIdPlaylistAndCancionIdCancion(playlistId, songId)) {
            log.debug("La canción {} ya está en la playlist {} (idempotente)", songId, playlistId);
            return;
        }

        PlaylistCancionEntity playlistCancion = new PlaylistCancionEntity();
        playlistCancion.setPlaylist(playlist);
        playlistCancion.setCancion(song);
        playlistCancion.setFechaAgregado(LocalDateTime.now());
        playlistCancionRepository.save(playlistCancion);

        log.info("Canción {} agregada exitosamente a playlist {}", songId, playlistId);
    }

    @Override
    @Transactional
    public void removeSongFromPlaylist(Long playlistId, Long songId) {
        log.info("Iniciando proceso de eliminar canción {} de playlist {}", songId, playlistId);

        // Idempotente: quitar algo que no estaba no produce error (no 404).
        playlistCancionRepository.deleteByPlaylistIdPlaylistAndCancionIdCancion(playlistId, songId);

        log.info("Canción {} eliminada de la playlist {} (si existía)", songId, playlistId);
    }


    private PlaylistResponseDTO convertToDTO(PlaylistEntity playlist, int songCount) {
        PlaylistResponseDTO dto = new PlaylistResponseDTO();
        dto.setPlaylistId(playlist.getIdPlaylist());
        dto.setName(playlist.getNombre());
        dto.setDescription(playlist.getDescripcion());
        dto.setIsPublic(playlist.getIsPublic());
        dto.setCoverImage(playlist.getImgPortada());
        dto.setCreationDate(playlist.getFechaCreacion());
        dto.setUserId(playlist.getUsuario().getIdUsuario());
        dto.setSongCount(songCount);
        return dto;
    }

    /**
     * Resuelve el número de canciones de varias playlists en una sola consulta (evita N+1).
     */
    private Map<Long, Integer> songCountsFor(List<PlaylistEntity> playlists) {
        List<Long> ids = playlists.stream().map(PlaylistEntity::getIdPlaylist).toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return playlistCancionRepository.countSongsByPlaylistIds(ids).stream()
                .collect(Collectors.toMap(
                        PlaylistSongCountProjection::getPlaylistId,
                        projection -> projection.getSongCount().intValue()));
    }
}