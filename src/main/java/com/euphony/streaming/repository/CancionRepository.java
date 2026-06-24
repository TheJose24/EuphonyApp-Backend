package com.euphony.streaming.repository;

import com.euphony.streaming.entity.CancionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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

}
