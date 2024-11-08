package com.euphony.streaming.repository;

import com.euphony.streaming.entity.ArtistaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArtistaRepository extends JpaRepository<ArtistaEntity, Long> {
    Optional<ArtistaEntity> findByNombre(String nombre);

    Boolean existsByNombre(String nombre);
}
