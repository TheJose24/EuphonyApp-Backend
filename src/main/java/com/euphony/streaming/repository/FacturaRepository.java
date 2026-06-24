package com.euphony.streaming.repository;

import com.euphony.streaming.entity.FacturaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacturaRepository extends JpaRepository<FacturaEntity, Long> {
}
