package com.euphony.streaming.repository;

import com.euphony.streaming.entity.AlbumEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlbumRepository extends JpaRepository<AlbumEntity, Long> {
    Optional<AlbumEntity> findByTitulo(String titulo);
    Boolean existsByTitulo(String titulo);
    Optional<AlbumEntity> findByArtistaNombre(String nombre);

    /**
     * Recupera los álbumes marcados como favoritos por un usuario, trayendo el artista en la
     * misma consulta mediante {@code LEFT JOIN FETCH}. Se ordenan por fecha de agregado
     * descendente (los más recientes primero).
     */
    @Query("SELECT a FROM FavoritosAlbumEntity f " +
            "JOIN f.album a " +
            "LEFT JOIN FETCH a.artista " +
            "WHERE f.usuario.idUsuario = :userId " +
            "ORDER BY f.fechaAgregado DESC")
    List<AlbumEntity> findFavoriteAlbumsByUser(@Param("userId") UUID userId);
}
