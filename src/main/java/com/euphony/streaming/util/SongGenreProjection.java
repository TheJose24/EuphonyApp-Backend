package com.euphony.streaming.util;

/**
 * Proyección que asocia el ID de una canción con el nombre de uno de sus géneros.
 * Se usa para traer los géneros de varias canciones en una sola consulta y así
 * evitar el problema N+1 al listar canciones.
 */
public interface SongGenreProjection {

    Long getSongId();
    String getGenreName();

}
