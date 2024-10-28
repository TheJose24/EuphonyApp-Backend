package com.euphony.streaming.service;

import com.euphony.streaming.dto.request.PlaylistRequestDTO;
import com.euphony.streaming.dto.response.PlaylistResponseDTO;
import com.euphony.streaming.entity.PlaylistEntity;
import com.euphony.streaming.entity.UsuarioEntity;
import com.euphony.streaming.exception.custom.PlaylistNotFoundException;
import com.euphony.streaming.repository.PlaylistRepository;
import com.euphony.streaming.repository.UsuarioRepository;
import com.euphony.streaming.service.implementation.PlaylistServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceImplTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private PlaylistServiceImpl playlistService;

    private PlaylistRequestDTO playlistRequest;
    private PlaylistEntity playlistEntity;
    private UsuarioEntity usuarioEntity;

    @BeforeEach
    void setUp() {
        // Configurar usuario de prueba
        usuarioEntity = new UsuarioEntity();
        usuarioEntity.setIdUsuario(UUID.randomUUID());
        usuarioEntity.setEmail("test@example.com");
        usuarioEntity.setUsername("testuser");

        // Configurar request DTO
        playlistRequest = new PlaylistRequestDTO();
        playlistRequest.setName("Playlist Test");
        playlistRequest.setDescription("Descripción de prueba");
        playlistRequest.setUserId(UUID.randomUUID());

        // Configurar entity
        playlistEntity = new PlaylistEntity();
        playlistEntity.setIdPlaylist(1L);
        playlistEntity.setNombre("Playlist Test");
        playlistEntity.setDescripcion("Descripción de prueba");
        playlistEntity.setFechaCreacion(LocalDate.from(LocalDateTime.now()));
        playlistEntity.setUsuario(usuarioEntity);
    }


    @Test
    void getPlaylistById_Success() {
        // Arrange
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlistEntity));

        // Act
        PlaylistResponseDTO response = playlistService.findPlaylistById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(playlistEntity.getIdPlaylist(), response.getPlaylistId());
        assertEquals(playlistEntity.getNombre(), response.getName());
    }

    @Test
    void getPlaylistById_ThrowsNotFoundException() {
        // Arrange
        when(playlistRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PlaylistNotFoundException.class, () ->
                playlistService.findPlaylistById(1L));
    }

    @Test
    void getAllPlaylists_Success() {
        // Arrange
        List<PlaylistEntity> playlists = Arrays.asList(playlistEntity);
        when(playlistRepository.findAll()).thenReturn(playlists);

        // Act
        List<PlaylistResponseDTO> response = playlistService.findAllPlaylists();

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(1, response.size());
        assertEquals(playlistEntity.getNombre(), response.get(0).getName());
    }


    @Test
    void deletePlaylist_Success() {
        // Arrange
        when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlistEntity));
        doNothing().when(playlistRepository).delete(any(PlaylistEntity.class));

        // Act & Assert
        assertDoesNotThrow(() -> playlistService.deletePlaylist(1L));
        verify(playlistRepository).delete(any(PlaylistEntity.class));
    }

    @Test
    void deletePlaylist_ThrowsNotFoundException() {
        // Arrange
        when(playlistRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PlaylistNotFoundException.class, () ->
                playlistService.deletePlaylist(1L));
    }
}