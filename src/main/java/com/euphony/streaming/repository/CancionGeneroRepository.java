package com.euphony.streaming.repository;

import com.euphony.streaming.entity.CancionEntity;
import com.euphony.streaming.entity.CancionGeneroEntity;
import com.euphony.streaming.util.SongGenreProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CancionGeneroRepository extends JpaRepository<CancionGeneroEntity, Long> {

    List<CancionGeneroEntity> findByCancion_IdCancion(Long idCancion);

    /**
     * Recupera, en una sola consulta, los géneros de todas las canciones indicadas.
     * Devuelve pares (songId, genreName) como proyección para agruparlos en memoria,
     * evitando el problema N+1 al listar canciones con sus géneros.
     */
    @Query("SELECT cg.cancion.idCancion AS songId, cg.genero.nombre AS genreName " +
            "FROM CancionGeneroEntity cg " +
            "WHERE cg.cancion.idCancion IN :songIds")
    List<SongGenreProjection> findGenresByCancionIds(@Param("songIds") Collection<Long> songIds);

    void deleteByCancion(CancionEntity idCancion);
}
