package com.euphony.streaming.repository;

import com.euphony.streaming.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, UUID> {
    UsuarioEntity findByEmail(String email);
    UsuarioEntity findByUsername(String username);
    UsuarioEntity findByEmailOrUsername(String email, String username);
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
}
