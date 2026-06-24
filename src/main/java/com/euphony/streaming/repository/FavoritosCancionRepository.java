package com.euphony.streaming.repository;

import com.euphony.streaming.entity.FavoritosCancionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoritosCancionRepository extends JpaRepository<FavoritosCancionEntity, Long> {
}
