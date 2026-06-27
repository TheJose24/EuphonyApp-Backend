package com.euphony.streaming.service;

import com.euphony.streaming.dto.request.FavoriteAlbumRequestDTO;
import com.euphony.streaming.dto.request.FavoriteSongRequestDTO;
import com.euphony.streaming.dto.response.AlbumResponseDTO;
import com.euphony.streaming.dto.response.SongResponseDTO;
import com.euphony.streaming.exception.custom.album.AlbumNotFoundException;
import com.euphony.streaming.exception.custom.favorite.FavoriteBadRequestException;
import com.euphony.streaming.exception.custom.song.SongNotFoundException;
import com.euphony.streaming.exception.custom.user.UserNotFoundException;
import com.euphony.streaming.repository.AlbumRepository;
import com.euphony.streaming.repository.CancionRepository;
import com.euphony.streaming.repository.FavoritosAlbumRepository;
import com.euphony.streaming.repository.FavoritosCancionRepository;
import com.euphony.streaming.repository.UsuarioRepository;
import com.euphony.streaming.service.implementation.FavoritesServiceImpl;
import com.euphony.streaming.service.interfaces.IAlbumService;
import com.euphony.streaming.service.interfaces.ISongService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FavoritesServiceImplTest {

    @Mock
    private FavoritosCancionRepository favoritosCancionRepository;
    @Mock
    private FavoritosAlbumRepository favoritosAlbumRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private CancionRepository cancionRepository;
    @Mock
    private AlbumRepository albumRepository;
    @Mock
    private ISongService songService;
    @Mock
    private IAlbumService albumService;

    @InjectMocks
    private FavoritesServiceImpl favoritesService;

    private static final UUID USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final Long SONG_ID = 10L;
    private static final Long ALBUM_ID = 2L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private FavoriteSongRequestDTO songRequest() {
        return FavoriteSongRequestDTO.builder().userId(USER_ID).songId(SONG_ID).build();
    }

    private FavoriteAlbumRequestDTO albumRequest() {
        return FavoriteAlbumRequestDTO.builder().userId(USER_ID).albumId(ALBUM_ID).build();
    }

    // ----------------------------- Canciones (likes) -----------------------------

    @Test
    void likeSong_savesWhenNotAlreadyLiked() {
        when(usuarioRepository.existsById(USER_ID)).thenReturn(true);
        when(cancionRepository.existsById(SONG_ID)).thenReturn(true);
        when(favoritosCancionRepository.existsByUsuario_IdUsuarioAndCancion_IdCancion(USER_ID, SONG_ID))
                .thenReturn(false);

        favoritesService.likeSong(songRequest());

        verify(favoritosCancionRepository).save(any());
    }

    @Test
    void likeSong_isIdempotentWhenAlreadyLiked() {
        when(usuarioRepository.existsById(USER_ID)).thenReturn(true);
        when(cancionRepository.existsById(SONG_ID)).thenReturn(true);
        when(favoritosCancionRepository.existsByUsuario_IdUsuarioAndCancion_IdCancion(USER_ID, SONG_ID))
                .thenReturn(true);

        // No lanza error y no inserta de nuevo.
        favoritesService.likeSong(songRequest());

        verify(favoritosCancionRepository, never()).save(any());
    }

    @Test
    void likeSong_throwsNotFoundWhenUserDoesNotExist() {
        when(usuarioRepository.existsById(USER_ID)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> favoritesService.likeSong(songRequest()));
        verify(favoritosCancionRepository, never()).save(any());
    }

    @Test
    void likeSong_throwsNotFoundWhenSongDoesNotExist() {
        when(usuarioRepository.existsById(USER_ID)).thenReturn(true);
        when(cancionRepository.existsById(SONG_ID)).thenReturn(false);

        assertThrows(SongNotFoundException.class, () -> favoritesService.likeSong(songRequest()));
        verify(favoritosCancionRepository, never()).save(any());
    }

    @Test
    void likeSong_throwsBadRequestWhenSongIdIsNull() {
        FavoriteSongRequestDTO request = FavoriteSongRequestDTO.builder().userId(USER_ID).songId(null).build();

        assertThrows(FavoriteBadRequestException.class, () -> favoritesService.likeSong(request));
        verify(usuarioRepository, never()).existsById(any(UUID.class));
    }

    @Test
    void unlikeSong_deletesIdempotently() {
        // Quitar un like (exista o no) no valida existencia ni lanza error.
        favoritesService.unlikeSong(songRequest());

        verify(favoritosCancionRepository).deleteByUsuario_IdUsuarioAndCancion_IdCancion(USER_ID, SONG_ID);
    }

    @Test
    void getFavoriteSongs_validatesUserAndDelegatesToSongService() {
        SongResponseDTO dto = SongResponseDTO.builder().songId(SONG_ID).artistName("Taylor Swift").build();
        when(usuarioRepository.existsById(USER_ID)).thenReturn(true);
        when(songService.findFavoriteSongsByUser(USER_ID)).thenReturn(List.of(dto));

        List<SongResponseDTO> result = favoritesService.getFavoriteSongs(USER_ID);

        assertEquals(1, result.size());
        assertEquals("Taylor Swift", result.get(0).getArtistName());
        verify(songService).findFavoriteSongsByUser(USER_ID);
    }

    @Test
    void getFavoriteSongs_throwsNotFoundWhenUserDoesNotExist() {
        when(usuarioRepository.existsById(USER_ID)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> favoritesService.getFavoriteSongs(USER_ID));
        verify(songService, never()).findFavoriteSongsByUser(any());
    }

    // ----------------------------- Álbumes (favoritos) -----------------------------

    @Test
    void favoriteAlbum_savesWhenNotAlreadyFavorite() {
        when(usuarioRepository.existsById(USER_ID)).thenReturn(true);
        when(albumRepository.existsById(ALBUM_ID)).thenReturn(true);
        when(favoritosAlbumRepository.existsByUsuario_IdUsuarioAndAlbum_IdAlbum(USER_ID, ALBUM_ID))
                .thenReturn(false);

        favoritesService.favoriteAlbum(albumRequest());

        verify(favoritosAlbumRepository).save(any());
    }

    @Test
    void favoriteAlbum_isIdempotentWhenAlreadyFavorite() {
        when(usuarioRepository.existsById(USER_ID)).thenReturn(true);
        when(albumRepository.existsById(ALBUM_ID)).thenReturn(true);
        when(favoritosAlbumRepository.existsByUsuario_IdUsuarioAndAlbum_IdAlbum(USER_ID, ALBUM_ID))
                .thenReturn(true);

        favoritesService.favoriteAlbum(albumRequest());

        verify(favoritosAlbumRepository, never()).save(any());
    }

    @Test
    void favoriteAlbum_throwsNotFoundWhenAlbumDoesNotExist() {
        when(usuarioRepository.existsById(USER_ID)).thenReturn(true);
        when(albumRepository.existsById(ALBUM_ID)).thenReturn(false);

        assertThrows(AlbumNotFoundException.class, () -> favoritesService.favoriteAlbum(albumRequest()));
        verify(favoritosAlbumRepository, never()).save(any());
    }

    @Test
    void unfavoriteAlbum_deletesIdempotently() {
        favoritesService.unfavoriteAlbum(albumRequest());

        verify(favoritosAlbumRepository).deleteByUsuario_IdUsuarioAndAlbum_IdAlbum(USER_ID, ALBUM_ID);
    }

    @Test
    void getFavoriteAlbums_validatesUserAndDelegatesToAlbumService() {
        AlbumResponseDTO dto = AlbumResponseDTO.builder().idAlbum(ALBUM_ID).titulo("1989").build();
        when(usuarioRepository.existsById(USER_ID)).thenReturn(true);
        when(albumService.findFavoriteAlbumsByUser(USER_ID)).thenReturn(List.of(dto));

        List<AlbumResponseDTO> result = favoritesService.getFavoriteAlbums(USER_ID);

        assertEquals(1, result.size());
        assertEquals("1989", result.get(0).getTitulo());
        verify(albumService).findFavoriteAlbumsByUser(USER_ID);
    }
}
