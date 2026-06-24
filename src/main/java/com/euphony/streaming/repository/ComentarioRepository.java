package com.euphony.streaming.repository;

import com.euphony.streaming.entity.ComentarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComentarioRepository extends JpaRepository<ComentarioEntity, Long> {

}
