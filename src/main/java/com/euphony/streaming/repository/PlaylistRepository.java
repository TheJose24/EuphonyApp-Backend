package com.euphony.streaming.repository;

import com.euphony.streaming.entity.PlaylistEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlaylistRepository extends JpaRepository<PlaylistEntity, Long> {
    List<PlaylistEntity> findByUsuarioIdUsuario(UUID userId);
}
