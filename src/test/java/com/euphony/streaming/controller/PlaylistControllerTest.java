package com.euphony.streaming.controller;

import com.euphony.streaming.dto.request.PlaylistRequestDTO;
import com.euphony.streaming.dto.response.PlaylistResponseDTO;
import com.euphony.streaming.service.interfaces.IPlaylistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlaylistControllerTest {

    @Mock
    private IPlaylistService playlistService;

    @InjectMocks
    private PlaylistController playlistController;

    private PlaylistRequestDTO playlistRequest;
    private PlaylistResponseDTO playlistResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Configurar datos de prueba
        playlistRequest = new PlaylistRequestDTO();
        playlistRequest.setName("Mi Playlist de Test");
        playlistRequest.setDescription("Descripción de prueba");

        playlistResponse = new PlaylistResponseDTO();
        playlistResponse.setPlaylistId(1L);
        playlistResponse.setName("Mi Playlist de Test");
        playlistResponse.setDescription("Descripción de prueba");
    }


    @Test
    void getPlaylistById_Success() {
        // Arrange
        Long playlistId = 1L;
        when(playlistService.findPlaylistById(playlistId))
                .thenReturn(playlistResponse);

        // Act
        ResponseEntity<PlaylistResponseDTO> response =
                playlistController.getPlaylistById(playlistId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(playlistResponse, response.getBody());
        verify(playlistService, times(1)).findPlaylistById(playlistId);
    }

    @Test
    void getAllPlaylists_Success() {
        // Arrange
        List<PlaylistResponseDTO> playlists =
                Arrays.asList(playlistResponse);
        when(playlistService.findAllPlaylists())
                .thenReturn(playlists);

        // Act
        ResponseEntity<List<PlaylistResponseDTO>> response =
                playlistController.getAllPlaylists();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(playlists, response.getBody());
        assertEquals(1, response.getBody().size());
        verify(playlistService, times(1)).findAllPlaylists();
    }


    @Test
    void deletePlaylist_Success() {
        // Arrange
        Long playlistId = 1L;
        doNothing().when(playlistService).deletePlaylist(playlistId);

        // Act
        ResponseEntity<Void> response = playlistController.deletePlaylist(playlistId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(playlistService, times(1)).deletePlaylist(playlistId);
    }
}