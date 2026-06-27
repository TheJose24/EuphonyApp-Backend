package com.euphony.streaming.util;

/**
 * Proyección (id de playlist, número de canciones) usada para resolver el {@code songCount}
 * de varias playlists en una sola consulta, evitando el problema N+1 en los listados.
 */
public interface PlaylistSongCountProjection {
    Long getPlaylistId();
    Long getSongCount();
}
