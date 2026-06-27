package com.euphony.streaming.repository;

import com.euphony.streaming.entity.PlaylistCancionEntity;
import com.euphony.streaming.entity.PlaylistCancionId;
import com.euphony.streaming.util.PlaylistSongCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PlaylistCancionRepository extends JpaRepository<PlaylistCancionEntity, PlaylistCancionId> {

    boolean existsByPlaylistIdPlaylistAndCancionIdCancion(Long playlistId, Long songId);
    void deleteByPlaylistIdPlaylistAndCancionIdCancion(Long playlistId, Long songId);
    List<PlaylistCancionEntity> findByPlaylistIdPlaylist(Long playlistId);

    /**
     * Número de canciones de una playlist. Para resolver el {@code songCount} de una sola playlist.
     */
    long countByPlaylistIdPlaylist(Long playlistId);

    /**
     * Cuenta las canciones de varias playlists en una sola consulta (evita N+1 en los listados).
     */
    @Query("SELECT pc.playlist.idPlaylist AS playlistId, COUNT(pc) AS songCount " +
            "FROM PlaylistCancionEntity pc " +
            "WHERE pc.playlist.idPlaylist IN :playlistIds " +
            "GROUP BY pc.playlist.idPlaylist")
    List<PlaylistSongCountProjection> countSongsByPlaylistIds(@Param("playlistIds") Collection<Long> playlistIds);
}
