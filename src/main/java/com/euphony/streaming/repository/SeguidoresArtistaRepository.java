package com.euphony.streaming.repository;

import com.euphony.streaming.entity.SeguidoresArtistaEntity;
import com.euphony.streaming.util.ArtistFollowerCount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SeguidoresArtistaRepository extends JpaRepository<SeguidoresArtistaEntity, Long> {

    List<SeguidoresArtistaEntity> findByUsuario_IdUsuario(UUID usuarioId);
    List<SeguidoresArtistaEntity> findByArtista_IdArtista(Long artistaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    boolean existsByUsuario_IdUsuarioAndArtista_IdArtista(UUID usuarioId, Long artistaId);
    void deleteByUsuarioIdUsuarioAndArtistaIdArtista(UUID usuarioId, Long artistaId);

    @Query("SELECT sa.artista.idArtista AS idArtista, COUNT(sa.usuario.idUsuario) AS followerCount " +
            "FROM SeguidoresArtistaEntity sa " +
            "WHERE sa.artista.isVerified = false " +
            "GROUP BY sa.artista.idArtista " +
            "HAVING COUNT(sa.usuario.idUsuario) >= :minFollowers")
    List<ArtistFollowerCount> findArtistsEligibleForVerification(@Param("minFollowers") Long minFollowers);
}
