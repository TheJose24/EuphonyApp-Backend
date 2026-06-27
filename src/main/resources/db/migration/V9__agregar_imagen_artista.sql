-- Agregar la columna de imagen a la tabla ARTISTA.
-- Guarda la ruta relativa servida por /uploads/images/... (igual que ALBUM.portada).
-- Nullable: los artistas sin imagen quedan en NULL y el frontend cae al avatar con inicial.
ALTER TABLE ARTISTA
    ADD COLUMN imagen VARCHAR(255) DEFAULT NULL;
