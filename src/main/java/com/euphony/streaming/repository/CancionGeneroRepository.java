package com.euphony.streaming.repository;

import com.euphony.streaming.entity.CancionEntity;
import com.euphony.streaming.entity.CancionGeneroEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CancionGeneroRepository extends JpaRepository<CancionGeneroEntity, Long> {

    List<CancionGeneroEntity> findByCancion_IdCancion(Long idCancion);

    void deleteByCancion(CancionEntity idCancion);
}
