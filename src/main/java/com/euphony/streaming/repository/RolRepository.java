package com.euphony.streaming.repository;

import com.euphony.streaming.entity.RolEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<RolEntity, Long> {
    Optional<RolEntity> findByNameRol(String nombreRol);
}
