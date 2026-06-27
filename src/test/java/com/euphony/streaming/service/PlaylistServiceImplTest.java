package com.euphony.streaming.service;

import com.euphony.streaming.dto.response.SongResponseDTO;
import com.euphony.streaming.entity.CancionEntity;
import com.euphony.streaming.entity.PlaylistEntity;
import com.euphony.streaming.exception.custom.playlist.PlaylistNotFoundException;
import com.euphony.streaming.exception.custom.song.SongNotFoundException;
import com.euphony.streaming.repository.CancionRepository;
import com.euphony.streaming.repository.PlaylistCancionRepository;
import com.euphony.streaming.repository.PlaylistRepository;
import com.euphony.streaming.repository.UsuarioRepository;
import com.euphony.streaming.service.implementation.PlaylistServiceImpl;
import com.euphony.streaming.service.interfaces.ISongService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlaylistServiceImplTest {

    @Mock
    private PlaylistRepository playlistRepository;
    @Mock
    private PlaylistCancionRepository playlistCancionRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private CancionRepository songRepository;
    @Mock
    private ISongService songService;

    @InjectMocks
    private PlaylistServiceImpl playlistService;

    private static final Long PLAYLIST_ID = 5L;
    private static final Long SONG_ID = 10L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private PlaylistEntity playlist() {
        PlaylistEntity p = new PlaylistEntity();
        p.setIdPlaylist(PLAYLIST_ID);
        return p;
    }

    private CancionEntity song() {
        CancionEntity c = new CancionEntity();
        c.setIdCancion(SONG_ID);
        return c;
    }

    @Test
    void addSongToPlaylist_savesWhenNotPresent() {
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(playlist()));
        when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song()));
        when(playlistCancionRepository.existsByPlaylistIdPlaylistAndCancionIdCancion(PLAYLIST_ID, SONG_ID))
                .thenReturn(false);

        playlistService.addSongToPlaylist(PLAYLIST_ID, SONG_ID);

        verify(playlistCancionRepository).save(any());
    }

    @Test
    void addSongToPlaylist_isIdempotentWhenAlreadyPresent() {
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(playlist()));
        when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song()));
        when(playlistCancionRepository.existsByPlaylistIdPlaylistAndCancionIdCancion(PLAYLIST_ID, SONG_ID))
                .thenReturn(true);

        // No lanza error y no inserta de nuevo.
        playlistService.addSongToPlaylist(PLAYLIST_ID, SONG_ID);

        verify(playlistCancionRepository, never()).save(any());
    }

    @Test
    void addSongToPlaylist_throwsWhenPlaylistDoesNotExist() {
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.empty());

        assertThrows(PlaylistNotFoundException.class,
                () -> playlistService.addSongToPlaylist(PLAYLIST_ID, SONG_ID));
        verify(playlistCancionRepository, never()).save(any());
    }

    @Test
    void addSongToPlaylist_throwsWhenSongDoesNotExist() {
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(playlist()));
        when(songRepository.findById(SONG_ID)).thenReturn(Optional.empty());

        assertThrows(SongNotFoundException.class,
                () -> playlistService.addSongToPlaylist(PLAYLIST_ID, SONG_ID));
        verify(playlistCancionRepository, never()).save(any());
    }

    @Test
    void removeSongFromPlaylist_deletesIdempotently() {
        // Quitar una canción (esté o no) no valida existencia ni lanza error.
        playlistService.removeSongFromPlaylist(PLAYLIST_ID, SONG_ID);

        verify(playlistCancionRepository)
                .deleteByPlaylistIdPlaylistAndCancionIdCancion(PLAYLIST_ID, SONG_ID);
    }

    @Test
    void getPlaylistSongs_validatesPlaylistAndDelegatesToSongService() {
        SongResponseDTO dto = SongResponseDTO.builder().songId(SONG_ID).artistName("Taylor Swift").build();
        when(playlistRepository.existsById(PLAYLIST_ID)).thenReturn(true);
        when(songService.findSongsByPlaylist(PLAYLIST_ID)).thenReturn(List.of(dto));

        List<SongResponseDTO> result = playlistService.getPlaylistSongs(PLAYLIST_ID);

        assertEquals(1, result.size());
        assertEquals("Taylor Swift", result.get(0).getArtistName());
        verify(songService).findSongsByPlaylist(PLAYLIST_ID);
    }

    @Test
    void getPlaylistSongs_throwsWhenPlaylistDoesNotExist() {
        when(playlistRepository.existsById(PLAYLIST_ID)).thenReturn(false);

        assertThrows(PlaylistNotFoundException.class,
                () -> playlistService.getPlaylistSongs(PLAYLIST_ID));
        verify(songService, never()).findSongsByPlaylist(anyLong());
    }
}
