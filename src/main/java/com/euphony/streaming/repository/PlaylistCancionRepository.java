package com.euphony.streaming.repository;

import com.euphony.streaming.entity.PlaylistCancionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistCancionRepository extends JpaRepository<PlaylistCancionEntity, Long> {

}
