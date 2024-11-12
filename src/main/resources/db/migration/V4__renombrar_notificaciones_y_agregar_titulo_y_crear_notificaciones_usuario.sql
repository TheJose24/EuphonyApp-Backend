-- 1. Renombrar la tabla NOTIFICACIONES a NOTIFICACIONES_ARTISTA
ALTER TABLE NOTIFICACIONES RENAME TO NOTIFICACIONES_ARTISTA;

-- 2. Añadir el campo 'titulo' a la tabla NOTIFICACIONES_ARTISTA
ALTER TABLE NOTIFICACIONES_ARTISTA
    ADD COLUMN titulo VARCHAR(255) NOT NULL DEFAULT '';

-- 3. Cambiar la FOREIGN KEY de id_usuario a id_artista
ALTER TABLE NOTIFICACIONES_ARTISTA
DROP CONSTRAINT fk_notificaciones_usuario, -- Usar DROP CONSTRAINT en lugar de DROP FOREIGN KEY
    DROP COLUMN id_usuario,
    ADD COLUMN id_artista BIGINT NOT NULL,
    ADD CONSTRAINT fk_notificaciones_artista FOREIGN KEY (id_artista) REFERENCES ARTISTA(id_artista);

-- 4. Crear la nueva tabla NOTIFICACIONES_USUARIO
CREATE TABLE NOTIFICACIONES_USUARIO (
                                        id_notificacion_usuario SERIAL PRIMARY KEY,
                                        id_usuario UUID NOT NULL,
                                        titulo VARCHAR(255) NOT NULL DEFAULT '',
                                        mensaje VARCHAR(1000) NOT NULL,
                                        leido BOOLEAN NOT NULL DEFAULT FALSE,
                                        fecha_envio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        contenido VARCHAR(1000) NOT NULL DEFAULT '',
                                        FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario)
);

-- 5. Crear índices para optimizar consultas
CREATE INDEX idx_notificaciones_artista_artista ON NOTIFICACIONES_ARTISTA(id_artista);
CREATE INDEX idx_notificaciones_usuario_usuario ON NOTIFICACIONES_USUARIO(id_usuario);
