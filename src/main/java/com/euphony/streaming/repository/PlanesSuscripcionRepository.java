package com.euphony.streaming.repository;

import com.euphony.streaming.entity.BoletaEntity;
import com.euphony.streaming.entity.PlanesSuscripcionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanesSuscripcionRepository extends JpaRepository<PlanesSuscripcionEntity, Long> {

    Boolean existsByNombrePlan(String nombre);

}
