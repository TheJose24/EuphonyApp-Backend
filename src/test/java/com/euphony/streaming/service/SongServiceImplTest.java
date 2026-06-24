package com.euphony.streaming.service;

import com.euphony.streaming.dto.response.SongResponseDTO;
import com.euphony.streaming.entity.AlbumEntity;
import com.euphony.streaming.entity.ArtistaEntity;
import com.euphony.streaming.entity.CancionEntity;
import com.euphony.streaming.repository.AlbumRepository;
import com.euphony.streaming.repository.ArtistaRepository;
import com.euphony.streaming.repository.CancionGeneroRepository;
import com.euphony.streaming.repository.CancionRepository;
import com.euphony.streaming.repository.GeneroRepository;
import com.euphony.streaming.service.implementation.SongServiceImpl;
import com.euphony.streaming.service.interfaces.IFileStorageService;
import com.euphony.streaming.service.interfaces.ISongMetadataService;
import com.euphony.streaming.util.SongGenreProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SongServiceImplTest {

    @Mock
    private CancionRepository cancionRepository;

    @Mock
    private GeneroRepository generoRepository;

    @Mock
    private CancionGeneroRepository cancionGeneroRepository;

    @Mock
    private ArtistaRepository artistaRepository;

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ISongMetadataService songMetadataService;

    @Mock
    private IFileStorageService fileStorageService;

    @InjectMocks
    private SongServiceImpl songService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Por defecto, las canciones no tienen géneros asociados (no es el foco de la mayoría de pruebas).
        when(cancionGeneroRepository.findByCancion_IdCancion(anyLong())).thenReturn(List.of());
    }

    private SongGenreProjection genreProjection(Long songId, String genreName) {
        return new SongGenreProjection() {
            @Override
            public Long getSongId() {
                return songId;
            }

            @Override
            public String getGenreName() {
                return genreName;
            }
        };
    }

    private ArtistaEntity buildArtist(Long id, String name) {
        ArtistaEntity artist = new ArtistaEntity();
        artist.setIdArtista(id);
        artist.setNombre(name);
        return artist;
    }

    private AlbumEntity buildAlbum(Long id, String title, String cover) {
        AlbumEntity album = new AlbumEntity();
        album.setIdAlbum(id);
        album.setTitulo(title);
        album.setPortada(cover);
        return album;
    }

    private CancionEntity buildSong(Long id, ArtistaEntity artist, AlbumEntity album) {
        CancionEntity song = new CancionEntity();
        song.setIdCancion(id);
        song.setTitulo("Mi Cancion Favorita");
        song.setArtista(artist);
        song.setAlbum(album);
        return song;
    }

    @Test
    void findAllSongs_populatesArtistAndAlbumNames() {
        // Arrange
        ArtistaEntity artist = buildArtist(1L, "Taylor Swift");
        AlbumEntity album = buildAlbum(2L, "1989", "/uploads/images/album_1989.jpg");
        CancionEntity song = buildSong(10L, artist, album);

        when(cancionRepository.findAllWithArtistAndAlbum()).thenReturn(List.of(song));

        // Act
        List<SongResponseDTO> result = songService.findAllSongs();

        // Assert
        assertEquals(1, result.size());
        SongResponseDTO dto = result.get(0);
        assertEquals(1L, dto.getArtistId());
        assertEquals("Taylor Swift", dto.getArtistName());
        assertEquals(2L, dto.getAlbumId());
        assertEquals("1989", dto.getAlbumTitle());
        assertEquals("/uploads/images/album_1989.jpg", dto.getAlbumCover());
    }

    @Test
    void findAllSongs_albumFieldsAreNullWhenSongHasNoAlbum() {
        // Arrange
        ArtistaEntity artist = buildArtist(1L, "Taylor Swift");
        CancionEntity song = buildSong(11L, artist, null);

        when(cancionRepository.findAllWithArtistAndAlbum()).thenReturn(List.of(song));

        // Act
        List<SongResponseDTO> result = songService.findAllSongs();

        // Assert
        SongResponseDTO dto = result.get(0);
        assertEquals("Taylor Swift", dto.getArtistName());
        assertNull(dto.getAlbumId());
        assertNull(dto.getAlbumTitle());
        assertNull(dto.getAlbumCover());
    }

    @Test
    void findAllSongs_groupsGenresPerSongFromSingleQuery() {
        // Arrange
        ArtistaEntity artist = buildArtist(1L, "Taylor Swift");
        AlbumEntity album = buildAlbum(2L, "1989", "/uploads/images/album_1989.jpg");
        CancionEntity songWithGenres = buildSong(10L, artist, album);
        CancionEntity songWithoutGenres = buildSong(11L, artist, null);

        when(cancionRepository.findAllWithArtistAndAlbum())
                .thenReturn(List.of(songWithGenres, songWithoutGenres));
        // Una sola consulta batcheada devuelve los pares (songId, genreName), desordenados a propósito.
        when(cancionGeneroRepository.findGenresByCancionIds(anyCollection()))
                .thenReturn(List.of(
                        genreProjection(10L, "Rock"),
                        genreProjection(10L, "Pop")));

        // Act
        List<SongResponseDTO> result = songService.findAllSongs();

        // Assert
        SongResponseDTO withGenres = result.stream()
                .filter(dto -> dto.getSongId().equals(10L)).findFirst().orElseThrow();
        SongResponseDTO withoutGenres = result.stream()
                .filter(dto -> dto.getSongId().equals(11L)).findFirst().orElseThrow();

        // Los géneros se devuelven en orden alfabético determinista, no en el de llegada.
        assertEquals(List.of("Pop", "Rock"), List.copyOf(withGenres.getGenres()));
        assertEquals(Set.of(), withoutGenres.getGenres());
    }

    @Test
    void findAllSongs_skipsGenreQueryWhenNoSongs() {
        // Arrange: no hay canciones.
        when(cancionRepository.findAllWithArtistAndAlbum()).thenReturn(List.of());

        // Act
        List<SongResponseDTO> result = songService.findAllSongs();

        // Assert: lista vacía y la consulta de géneros nunca se ejecuta (no se genera un IN ()).
        assertTrue(result.isEmpty());
        verify(cancionGeneroRepository, never()).findGenresByCancionIds(anyCollection());
    }

    @Test
    void searchSongById_populatesArtistAndAlbumNames() {
        // Arrange
        ArtistaEntity artist = buildArtist(1L, "Taylor Swift");
        AlbumEntity album = buildAlbum(2L, "1989", "/uploads/images/album_1989.jpg");
        CancionEntity song = buildSong(10L, artist, album);

        when(cancionRepository.findByIdWithArtistAndAlbum(10L)).thenReturn(java.util.Optional.of(song));

        // Act
        SongResponseDTO dto = songService.searchSongById(10L);

        // Assert
        assertEquals("Taylor Swift", dto.getArtistName());
        assertEquals("1989", dto.getAlbumTitle());
        assertEquals("/uploads/images/album_1989.jpg", dto.getAlbumCover());
    }
}
