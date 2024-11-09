package com.euphony.streaming.repository;

import com.euphony.streaming.entity.SeguidoresArtistaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SeguidoresArtistaRepository extends JpaRepository<SeguidoresArtistaEntity, Long> {

    List<SeguidoresArtistaEntity> findByUsuario_IdUsuario(UUID usuarioId);
    List<SeguidoresArtistaEntity> findByArtista_IdArtista(Long artistaId);
    boolean existsByUsuarioIdUsuarioAndArtistaIdArtista(UUID usuarioId, Long artistaId);
    void deleteByUsuarioIdUsuarioAndArtistaIdArtista(UUID usuarioId, Long artistaId);
}
