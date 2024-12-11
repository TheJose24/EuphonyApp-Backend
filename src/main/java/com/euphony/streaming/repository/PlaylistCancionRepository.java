package com.euphony.streaming.repository;

import com.euphony.streaming.entity.PlaylistCancionEntity;
import com.euphony.streaming.entity.PlaylistCancionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistCancionRepository extends JpaRepository<PlaylistCancionEntity, PlaylistCancionId> {

    boolean existsByPlaylistIdPlaylistAndCancionIdCancion(Long playlistId, Long songId);
    void deleteByPlaylistIdPlaylistAndCancionIdCancion(Long playlistId, Long songId);
    List<PlaylistCancionEntity> findByPlaylistIdPlaylist(Long playlistId);

}
