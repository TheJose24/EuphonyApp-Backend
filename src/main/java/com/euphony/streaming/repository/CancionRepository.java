package com.euphony.streaming.repository;

import com.euphony.streaming.entity.CancionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CancionRepository extends JpaRepository<CancionEntity, Long> {

}
