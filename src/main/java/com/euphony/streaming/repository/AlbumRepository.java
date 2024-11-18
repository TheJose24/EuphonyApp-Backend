package com.euphony.streaming.repository;

import com.euphony.streaming.entity.AlbumEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlbumRepository extends JpaRepository<AlbumEntity, Long> {
    Optional<AlbumEntity> findByTitulo(String titulo);
    Boolean existsByTitulo(String titulo);
}
