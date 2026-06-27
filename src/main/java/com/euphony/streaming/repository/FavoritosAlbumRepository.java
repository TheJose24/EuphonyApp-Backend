package com.euphony.streaming.repository;

import com.euphony.streaming.entity.FavoritosAlbumEntity;
import com.euphony.streaming.entity.FavoritosAlbumId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FavoritosAlbumRepository extends JpaRepository<FavoritosAlbumEntity, FavoritosAlbumId> {

    /**
     * Indica si el usuario ya marcó el álbum como favorito. Se usa para que marcar
     * favorito sea idempotente (no se inserta dos veces).
     */
    boolean existsByUsuario_IdUsuarioAndAlbum_IdAlbum(UUID usuarioId, Long albumId);

    /**
     * Elimina el álbum favorito del usuario si existe. Quitar un favorito inexistente
     * no produce error (idempotente).
     */
    void deleteByUsuario_IdUsuarioAndAlbum_IdAlbum(UUID usuarioId, Long albumId);
}
