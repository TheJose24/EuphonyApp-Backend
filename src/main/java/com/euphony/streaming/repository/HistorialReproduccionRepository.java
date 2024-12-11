package com.euphony.streaming.repository;

import com.euphony.streaming.entity.HistorialReproduccionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HistorialReproduccionRepository extends JpaRepository<HistorialReproduccionEntity, Long> {

    List<HistorialReproduccionEntity> findByUsuarioIdUsuario(UUID idUsuario, Pageable pageable);
    List<HistorialReproduccionEntity> findByUsuarioIdUsuarioOrderByFechaReproduccionDesc(UUID idUsuario);
}
