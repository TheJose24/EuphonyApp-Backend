package com.euphony.streaming.repository;

import com.euphony.streaming.entity.GeneroEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GeneroRepository extends JpaRepository<GeneroEntity, Long> {
    Optional<GeneroEntity> findByNombre(String nombre);
    Boolean existsByNombre(String nombre);
}
