package com.euphony.streaming.repository;

import com.euphony.streaming.entity.CancionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CancionRepository extends JpaRepository<CancionEntity, Long> {

    /**
     * Recupera todas las canciones trayendo el artista y el álbum en la misma consulta
     * mediante {@code LEFT JOIN FETCH}, evitando el problema N+1 al resolver sus nombres.
     */
    @Query("SELECT c FROM CancionEntity c " +
            "LEFT JOIN FETCH c.artista " +
            "LEFT JOIN FETCH c.album")
    List<CancionEntity> findAllWithArtistAndAlbum();

    /**
     * Recupera una canción por su ID trayendo el artista y el álbum en la misma consulta
     * mediante {@code LEFT JOIN FETCH}.
     */
    @Query("SELECT c FROM CancionEntity c " +
            "LEFT JOIN FETCH c.artista " +
            "LEFT JOIN FETCH c.album " +
            "WHERE c.idCancion = :songId")
    Optional<CancionEntity> findByIdWithArtistAndAlbum(@Param("songId") Long songId);

    /**
     * Recupera las canciones de un álbum trayendo el artista y el álbum en la misma
     * consulta mediante {@code LEFT JOIN FETCH}, evitando el problema N+1.
     */
    @Query("SELECT c FROM CancionEntity c " +
            "LEFT JOIN FETCH c.artista " +
            "LEFT JOIN FETCH c.album " +
            "WHERE c.album.idAlbum = :albumId")
    List<CancionEntity> findByAlbumIdWithArtistAndAlbum(@Param("albumId") Long albumId);

    /**
     * Recupera las canciones de un artista trayendo el artista y el álbum en la misma
     * consulta mediante {@code LEFT JOIN FETCH}, evitando el problema N+1.
     */
    @Query("SELECT c FROM CancionEntity c " +
            "LEFT JOIN FETCH c.artista " +
            "LEFT JOIN FETCH c.album " +
            "WHERE c.artista.idArtista = :artistId")
    List<CancionEntity> findByArtistIdWithArtistAndAlbum(@Param("artistId") Long artistId);

    /**
     * Recupera las canciones marcadas como favoritas ("me gusta") por un usuario, trayendo
     * el artista y el álbum en la misma consulta mediante {@code LEFT JOIN FETCH} para evitar
     * el problema N+1. Se ordenan por fecha de agregado descendente (las más recientes primero).
     */
    @Query("SELECT c FROM FavoritosCancionEntity f " +
            "JOIN f.cancion c " +
            "LEFT JOIN FETCH c.artista " +
            "LEFT JOIN FETCH c.album " +
            "WHERE f.usuario.idUsuario = :userId " +
            "ORDER BY f.fechaAgregado DESC")
    List<CancionEntity> findFavoriteSongsByUser(@Param("userId") UUID userId);

    /**
     * Recupera las canciones de una playlist trayendo el artista y el álbum en la misma
     * consulta mediante {@code LEFT JOIN FETCH} (sin N+1), en el orden en que se añadieron.
     */
    @Query("SELECT c FROM PlaylistCancionEntity pc " +
            "JOIN pc.cancion c " +
            "LEFT JOIN FETCH c.artista " +
            "LEFT JOIN FETCH c.album " +
            "WHERE pc.playlist.idPlaylist = :playlistId " +
            "ORDER BY pc.fechaAgregado ASC, c.idCancion ASC")
    List<CancionEntity> findSongsByPlaylistId(@Param("playlistId") Long playlistId);

}
