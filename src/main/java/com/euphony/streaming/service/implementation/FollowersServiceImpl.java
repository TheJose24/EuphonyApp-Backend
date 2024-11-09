package com.euphony.streaming.service.implementation;

import com.euphony.streaming.dto.request.FollowersArtistRequestDTO;
import com.euphony.streaming.dto.response.FollowersArtistResponseDTO;
import com.euphony.streaming.entity.SeguidoresArtistaEntity;
import com.euphony.streaming.exception.custom.artist.ArtistNotFoundException;
import com.euphony.streaming.exception.custom.follow.FollowAlreadyExistsException;
import com.euphony.streaming.exception.custom.follow.FollowBadRequestException;
import com.euphony.streaming.exception.custom.follow.FollowNotFoundException;
import com.euphony.streaming.exception.custom.user.UserNotFoundException;
import com.euphony.streaming.repository.ArtistaRepository;
import com.euphony.streaming.repository.SeguidoresArtistaRepository;
import com.euphony.streaming.repository.UsuarioRepository;
import com.euphony.streaming.service.interfaces.IFollowersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio que gestiona las operaciones de seguidores entre usuarios y artistas.
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__(@Lazy))
public class FollowersServiceImpl implements IFollowersService {

    private static final String USUARIO_NO_ENCONTRADO = "No se encontró el usuario con ID: %s";
    private static final String ARTISTA_NO_ENCONTRADO = "No se encontró el artista con ID: %d";
    private static final String USUARIO_YA_SIGUE = "El usuario con ID: %s ya sigue al artista con ID: %d";
    private static final String USUARIO_NO_SIGUE = "El usuario con ID: %s no sigue al artista con ID: %d";
    private static final String ID_USUARIO_NULO = "El ID del usuario no puede ser nulo";
    private static final String ID_ARTISTA_INVALIDO = "El ID del artista debe ser un número positivo";
    private static final String REQUEST_NULO = "La solicitud no puede ser nula";

    private final SeguidoresArtistaRepository seguidoresArtistaRepository;
    private final ArtistaRepository artistaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    @Override
    public void followArtist(FollowersArtistRequestDTO request) {
        log.debug("Iniciando proceso de seguir artista. Usuario ID: {}, Artista ID: {}",
                request.getUserId(), request.getArtistId());

        try {
            validateRequest(request);
            validateUserAndArtistExist(request.getUserId(), request.getArtistId());

            if (isAlreadyFollowing(request.getUserId(), request.getArtistId())) {
                String errorMessage = String.format(USUARIO_YA_SIGUE, request.getUserId(), request.getArtistId());
                log.warn(errorMessage);
                throw new FollowAlreadyExistsException(errorMessage, HttpStatus.CONFLICT);
            }

            createAndSaveFollowEntity(request);
            log.info("Usuario {} ahora sigue al artista {}", request.getUserId(), request.getArtistId());

        } catch (Exception e) {
            log.error("Error al seguir artista: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    @Override
    public void unfollowArtist(FollowersArtistRequestDTO request) {
        log.debug("Iniciando proceso de dejar de seguir artista. Usuario ID: {}, Artista ID: {}",
                request.getUserId(), request.getArtistId());

        try {
            validateRequest(request);
            validateUserAndArtistExist(request.getUserId(), request.getArtistId());

            if (!isAlreadyFollowing(request.getUserId(), request.getArtistId())) {
                String errorMessage = String.format(USUARIO_NO_SIGUE, request.getUserId(), request.getArtistId());
                log.warn(errorMessage);
                throw new FollowNotFoundException(errorMessage, HttpStatus.NOT_FOUND);
            }

            deleteFollowRelation(request.getUserId(), request.getArtistId());
            log.info("Usuario {} ha dejado de seguir al artista {}", request.getUserId(), request.getArtistId());

        } catch (Exception e) {
            log.error("Error al dejar de seguir artista: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<FollowersArtistResponseDTO> getFollowersByArtist(Long artistId) {
        log.debug("Obteniendo seguidores para el artista ID: {}", artistId);
        try {
            validateArtistId(artistId);
            validateArtistExists(artistId);

            List<FollowersArtistResponseDTO> followers = seguidoresArtistaRepository
                    .findByArtista_IdArtista(artistId)
                    .stream()
                    .map(this::mapToFollowersResponseDTO)
                    .toList();

            log.info("Se encontraron {} seguidores para el artista {}", followers.size(), artistId);
            return followers;

        } catch (Exception e) {
            log.error("Error al obtener seguidores del artista: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<FollowersArtistResponseDTO> getFollowersByUser(UUID userId) {
        log.debug("Obteniendo artistas seguidos por el usuario ID: {}", userId);
        try {
            validateUserId(userId);
            validateUserExists(userId);

            List<FollowersArtistResponseDTO> following = seguidoresArtistaRepository
                    .findByUsuario_IdUsuario(userId)
                    .stream()
                    .map(this::mapToFollowersResponseDTO)
                    .toList();

            log.info("Se encontraron {} artistas seguidos por el usuario {}", following.size(), userId);
            return following;

        } catch (Exception e) {
            log.error("Error al obtener artistas seguidos por el usuario: {}", e.getMessage());
            throw e;
        }
    }

    private void validateRequest(FollowersArtistRequestDTO request) {
        if (request == null) {
            log.error("La solicitud es nula");
            throw new FollowBadRequestException(REQUEST_NULO, HttpStatus.BAD_REQUEST);
        }
        validateUserId(request.getUserId());
        validateArtistId(request.getArtistId());
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            log.error("El ID del usuario es nulo");
            throw new FollowBadRequestException(ID_USUARIO_NULO, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateArtistId(Long artistId) {
        if (artistId == null || artistId <= 0) {
            log.error("El ID del artista es inválido: {}", artistId);
            throw new FollowBadRequestException(ID_ARTISTA_INVALIDO, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateUserAndArtistExist(UUID userId, Long artistId) {
        validateUserExists(userId);
        validateArtistExists(artistId);
    }

    private void validateUserExists(UUID userId) {
        if (!usuarioRepository.existsById(userId)) {
            String errorMessage = String.format(USUARIO_NO_ENCONTRADO, userId);
            log.error(errorMessage);
            throw new UserNotFoundException(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    private void validateArtistExists(Long artistId) {
        if (!artistaRepository.existsById(artistId)) {
            String errorMessage = String.format(ARTISTA_NO_ENCONTRADO, artistId);
            log.error(errorMessage);
            throw new ArtistNotFoundException(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    private boolean isAlreadyFollowing(UUID userId, Long artistId) {
        return seguidoresArtistaRepository
                .existsByUsuarioIdUsuarioAndArtistaIdArtista(userId, artistId);
    }

    private void createAndSaveFollowEntity(FollowersArtistRequestDTO request) {
        try {
            SeguidoresArtistaEntity followEntity = new SeguidoresArtistaEntity();
            followEntity.setUsuario(usuarioRepository.getReferenceById(request.getUserId()));
            followEntity.setArtista(artistaRepository.getReferenceById(request.getArtistId()));
            followEntity.setFechaSeguimiento(LocalDateTime.now());

            seguidoresArtistaRepository.save(followEntity);
        } catch (Exception e) {
            log.error("Error al crear y guardar la relación de seguimiento: {}", e.getMessage());
            throw new FollowBadRequestException("Error al crear la relación de seguimiento", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void deleteFollowRelation(UUID userId, Long artistId) {
        try {
            seguidoresArtistaRepository
                    .deleteByUsuarioIdUsuarioAndArtistaIdArtista(userId, artistId);
        } catch (Exception e) {
            log.error("Error al eliminar la relación de seguimiento: {}", e.getMessage());
            throw new FollowBadRequestException("Error al eliminar la relación de seguimiento", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private FollowersArtistResponseDTO mapToFollowersResponseDTO(SeguidoresArtistaEntity entity) {
        return FollowersArtistResponseDTO.builder()
                .userId(entity.getUsuario().getIdUsuario())
                .userName(entity.getUsuario().getNombre())
                .artistId(entity.getArtista().getIdArtista())
                .artistName(entity.getArtista().getNombre())
                .followDate(entity.getFechaSeguimiento())
                .build();
    }
}