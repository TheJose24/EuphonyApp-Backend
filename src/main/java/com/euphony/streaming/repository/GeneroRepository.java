package com.euphony.streaming.repository;

import com.euphony.streaming.dto.response.GenreMostPlayedDTO;
import com.euphony.streaming.entity.GeneroEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GeneroRepository extends JpaRepository<GeneroEntity, Long> {
    Optional<GeneroEntity> findByNombre(String nombre);
    Boolean existsByNombre(String nombre);
    @Query("""
    SELECT new com.euphony.streaming.dto.response.GenreMostPlayedDTO(
        g.idGenero,
        g.nombre,
        SUM(c.numeroReproducciones))
    FROM GeneroEntity g
    JOIN CancionGeneroEntity cg ON g = cg.genero
    JOIN CancionEntity c ON c = cg.cancion
    GROUP BY g.idGenero, g.nombre
    ORDER BY SUM(c.numeroReproducciones) DESC
""")
    List<GenreMostPlayedDTO> findMostPlayedGenres(Pageable pageable);

}
