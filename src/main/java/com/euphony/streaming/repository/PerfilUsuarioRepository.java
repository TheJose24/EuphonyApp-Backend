package com.euphony.streaming.repository;

import com.euphony.streaming.entity.PerfilUsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PerfilUsuarioRepository extends JpaRepository<PerfilUsuarioEntity, Long> {

    Optional<PerfilUsuarioEntity> findByUsuarioIdUsuario(UUID idUsuario);

}
