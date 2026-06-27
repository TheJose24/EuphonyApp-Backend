-- Añade la fecha de agregado a la tabla puente PLAYLIST_CANCION para poder devolver las
-- canciones de una playlist en el orden en que se añadieron. Las filas existentes toman
-- CURRENT_TIMESTAMP; las nuevas las setea la aplicación al insertarse.
ALTER TABLE PLAYLIST_CANCION
    ADD COLUMN fecha_agregado TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
