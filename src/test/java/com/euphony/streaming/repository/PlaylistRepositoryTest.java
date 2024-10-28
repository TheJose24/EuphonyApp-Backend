package com.euphony.streaming.repository;

import com.euphony.streaming.entity.PlaylistEntity;
import com.euphony.streaming.entity.UsuarioEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PlaylistRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PlaylistRepository playlistRepository;

    private UsuarioEntity usuarioEntity;
    private PlaylistEntity playlistEntity;

    @BeforeEach
    void setUp() {
        // Crear y persistir usuario
        usuarioEntity = new UsuarioEntity();
        usuarioEntity.setEmail("test@example.com");
        usuarioEntity.setUsername("testuser");
        usuarioEntity = entityManager.persist(usuarioEntity);

        // Crear y persistir playlist
        playlistEntity = new PlaylistEntity();
        playlistEntity.setNombre("Test Playlist");
        playlistEntity.setDescripcion("Test Description");
        playlistEntity.setFechaCreacion(LocalDate.from(LocalDateTime.now()));
        playlistEntity.setUsuario(usuarioEntity);
        playlistEntity = entityManager.persist(playlistEntity);

        entityManager.flush();
    }

    @Test
    void findById_ReturnPlaylist() {
        // Act
        Optional<PlaylistEntity> found = playlistRepository.findById(playlistEntity.getIdPlaylist());

        // Assert
        assertTrue(found.isPresent());
        assertEquals(playlistEntity.getNombre(), found.get().getNombre());
    }

    @Test
    void save_CreateNewPlaylist() {
        // Arrange
        PlaylistEntity newPlaylist = new PlaylistEntity();
        newPlaylist.setNombre("New Playlist");
        newPlaylist.setDescripcion("New Description");
        newPlaylist.setFechaCreacion(LocalDate.from(LocalDateTime.now()));
        newPlaylist.setUsuario(usuarioEntity);

        // Act
        PlaylistEntity saved = playlistRepository.save(newPlaylist);

        // Assert
        assertNotNull(saved.getIdPlaylist());
        assertEquals("New Playlist", saved.getNombre());
    }

    @Test
    void findAll_ReturnAllPlaylists() {
        // Arrange
        PlaylistEntity secondPlaylist = new PlaylistEntity();
        secondPlaylist.setNombre("Second Playlist");
        secondPlaylist.setDescripcion("Second Description");
        secondPlaylist.setFechaCreacion(LocalDate.from(LocalDateTime.now()));
        secondPlaylist.setUsuario(usuarioEntity);
        entityManager.persist(secondPlaylist);
        entityManager.flush();

        // Act
        List<PlaylistEntity> playlists = playlistRepository.findAll();

        // Assert
        assertEquals(2, playlists.size());
    }

    @Test
    void delete_RemovePlaylist() {
        // Act
        playlistRepository.deleteById(playlistEntity.getIdPlaylist());
        Optional<PlaylistEntity> deleted = playlistRepository.findById(playlistEntity.getIdPlaylist());

        // Assert
        assertFalse(deleted.isPresent());
    }

}