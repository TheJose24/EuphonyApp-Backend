package com.euphony.streaming.service.implementation;

import com.euphony.streaming.dto.request.FavoriteAlbumRequestDTO;
import com.euphony.streaming.dto.request.FavoriteSongRequestDTO;
import com.euphony.streaming.dto.response.AlbumResponseDTO;
import com.euphony.streaming.dto.response.SongResponseDTO;
import com.euphony.streaming.entity.FavoritosAlbumEntity;
import com.euphony.streaming.entity.FavoritosCancionEntity;
import com.euphony.streaming.exception.custom.album.AlbumNotFoundException;
import com.euphony.streaming.exception.custom.favorite.FavoriteBadRequestException;
import com.euphony.streaming.exception.custom.song.SongNotFoundException;
import com.euphony.streaming.exception.custom.user.UserNotFoundException;
import com.euphony.streaming.repository.AlbumRepository;
import com.euphony.streaming.repository.CancionRepository;
import com.euphony.streaming.repository.FavoritosAlbumRepository;
import com.euphony.streaming.repository.FavoritosCancionRepository;
import com.euphony.streaming.repository.UsuarioRepository;
import com.euphony.streaming.service.interfaces.IAlbumService;
import com.euphony.streaming.service.interfaces.IFavoritesService;
import com.euphony.streaming.service.interfaces.ISongService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__(@Lazy))
public class FavoritesServiceImpl implements IFavoritesService {

    private static final String REQUEST_NULO = "La solicitud no puede ser nula";
    private static final String ID_USUARIO_NULO = "El ID del usuario no puede ser nulo";
    private static final String ID_CANCION_NULO = "El ID de la canción no puede ser nulo";
    private static final String ID_ALBUM_NULO = "El ID del álbum no puede ser nulo";
    private static final String USUARIO_NO_ENCONTRADO = "No se encontró el usuario con ID: %s";
    private static final String CANCION_NO_ENCONTRADA = "No se encontró la canción con ID: %d";
    private static final String ALBUM_NO_ENCONTRADO = "No se encontró el álbum con ID: %d";

    private final FavoritosCancionRepository favoritosCancionRepository;
    private final FavoritosAlbumRepository favoritosAlbumRepository;
    private final UsuarioRepository usuarioRepository;
    private final CancionRepository cancionRepository;
    private final AlbumRepository albumRepository;
    private final ISongService songService;
    private final IAlbumService albumService;

    // ----------------------------- Canciones (likes) -----------------------------

    @Transactional
    @Override
    public void likeSong(FavoriteSongRequestDTO request) {
        validateSongRequest(request);
        validateUserExists(request.getUserId());
        validateSongExists(request.getSongId());

        // Idempotente: si ya le dio like, no se inserta de nuevo.
        if (favoritosCancionRepository.existsByUsuario_IdUsuarioAndCancion_IdCancion(
                request.getUserId(), request.getSongId())) {
            log.debug("El usuario {} ya tiene como favorita la canción {} (idempotente)",
                    request.getUserId(), request.getSongId());
            return;
        }

        FavoritosCancionEntity favorito = new FavoritosCancionEntity();
        favorito.setUsuario(usuarioRepository.getReferenceById(request.getUserId()));
        favorito.setCancion(cancionRepository.getReferenceById(request.getSongId()));
        favorito.setFechaAgregado(LocalDateTime.now());
        favoritosCancionRepository.save(favorito);

        log.info("Usuario {} marcó como favorita la canción {}", request.getUserId(), request.getSongId());
    }

    @Transactional
    @Override
    public void unlikeSong(FavoriteSongRequestDTO request) {
        validateSongRequest(request);

        // Idempotente: borrar algo que no estaba no produce error.
        favoritosCancionRepository.deleteByUsuario_IdUsuarioAndCancion_IdCancion(
                request.getUserId(), request.getSongId());

        log.info("Usuario {} quitó de favoritas la canción {}", request.getUserId(), request.getSongId());
    }

    @Transactional(readOnly = true)
    @Override
    public List<SongResponseDTO> getFavoriteSongs(UUID userId) {
        validateUserId(userId);
        validateUserExists(userId);
        return songService.findFavoriteSongsByUser(userId);
    }

    // ----------------------------- Álbumes (favoritos) -----------------------------

    @Transactional
    @Override
    public void favoriteAlbum(FavoriteAlbumRequestDTO request) {
        validateAlbumRequest(request);
        validateUserExists(request.getUserId());
        validateAlbumExists(request.getAlbumId());

        // Idempotente: si ya es favorito, no se inserta de nuevo.
        if (favoritosAlbumRepository.existsByUsuario_IdUsuarioAndAlbum_IdAlbum(
                request.getUserId(), request.getAlbumId())) {
            log.debug("El usuario {} ya tiene como favorito el álbum {} (idempotente)",
                    request.getUserId(), request.getAlbumId());
            return;
        }

        FavoritosAlbumEntity favorito = new FavoritosAlbumEntity();
        favorito.setUsuario(usuarioRepository.getReferenceById(request.getUserId()));
        favorito.setAlbum(albumRepository.getReferenceById(request.getAlbumId()));
        favorito.setFechaAgregado(LocalDateTime.now());
        favoritosAlbumRepository.save(favorito);

        log.info("Usuario {} marcó como favorito el álbum {}", request.getUserId(), request.getAlbumId());
    }

    @Transactional
    @Override
    public void unfavoriteAlbum(FavoriteAlbumRequestDTO request) {
        validateAlbumRequest(request);

        // Idempotente: borrar algo que no estaba no produce error.
        favoritosAlbumRepository.deleteByUsuario_IdUsuarioAndAlbum_IdAlbum(
                request.getUserId(), request.getAlbumId());

        log.info("Usuario {} quitó de favoritos el álbum {}", request.getUserId(), request.getAlbumId());
    }

    @Transactional(readOnly = true)
    @Override
    public List<AlbumResponseDTO> getFavoriteAlbums(UUID userId) {
        validateUserId(userId);
        validateUserExists(userId);
        return albumService.findFavoriteAlbumsByUser(userId);
    }

    // ----------------------------- Validaciones -----------------------------

    private void validateSongRequest(FavoriteSongRequestDTO request) {
        if (request == null) {
            throw new FavoriteBadRequestException(REQUEST_NULO, HttpStatus.BAD_REQUEST);
        }
        validateUserId(request.getUserId());
        if (request.getSongId() == null) {
            throw new FavoriteBadRequestException(ID_CANCION_NULO, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateAlbumRequest(FavoriteAlbumRequestDTO request) {
        if (request == null) {
            throw new FavoriteBadRequestException(REQUEST_NULO, HttpStatus.BAD_REQUEST);
        }
        validateUserId(request.getUserId());
        if (request.getAlbumId() == null) {
            throw new FavoriteBadRequestException(ID_ALBUM_NULO, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            throw new FavoriteBadRequestException(ID_USUARIO_NULO, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateUserExists(UUID userId) {
        if (!usuarioRepository.existsById(userId)) {
            throw new UserNotFoundException(String.format(USUARIO_NO_ENCONTRADO, userId), HttpStatus.NOT_FOUND);
        }
    }

    private void validateSongExists(Long songId) {
        if (!cancionRepository.existsById(songId)) {
            throw new SongNotFoundException(String.format(CANCION_NO_ENCONTRADA, songId), HttpStatus.NOT_FOUND);
        }
    }

    private void validateAlbumExists(Long albumId) {
        if (!albumRepository.existsById(albumId)) {
            throw new AlbumNotFoundException(String.format(ALBUM_NO_ENCONTRADO, albumId), HttpStatus.NOT_FOUND);
        }
    }
}
