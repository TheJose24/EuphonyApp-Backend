package com.euphony.streaming.repository;

import com.euphony.streaming.entity.FavoritosCancionEntity;
import com.euphony.streaming.entity.FavoritosCancionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FavoritosCancionRepository extends JpaRepository<FavoritosCancionEntity, FavoritosCancionId> {

    /**
     * Indica si el usuario ya marcó la canción como favorita. Se usa para que dar like
     * sea idempotente (no se inserta dos veces).
     */
    boolean existsByUsuario_IdUsuarioAndCancion_IdCancion(UUID usuarioId, Long cancionId);

    /**
     * Elimina el "me gusta" del usuario sobre la canción si existe. Quitar un like
     * inexistente no produce error (idempotente).
     */
    void deleteByUsuario_IdUsuarioAndCancion_IdCancion(UUID usuarioId, Long cancionId);
}
