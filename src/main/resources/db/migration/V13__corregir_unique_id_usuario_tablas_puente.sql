-- Corrige el mismo bug latente del esquema inicial (V1) en el resto de tablas donde un usuario
-- debe poder tener MUCHAS filas: se crearon con `id_usuario UUID UNIQUE` (restricción UNIQUE sobre
-- la columna sola), lo que limitaría a CADA usuario a una única fila. Afecta a:
--   - SEGUIDORES_ARTISTA    (un usuario sigue a MUCHOS artistas)   <- ya en uso por el módulo de seguidores
--   - HISTORIAL_REPRODUCCION(un usuario tiene MUCHAS reproducciones)
--   - COMENTARIOS           (un usuario hace MUCHOS comentarios)
--   - CALIFICACIONES        (un usuario califica MUCHAS canciones)
--   - COLABORACION_PLAYLIST (un usuario colabora en MUCHAS playlists)
--
-- La unicidad correcta ya la garantizan las claves primarias compuestas (o el PK propio) de cada
-- tabla. NO se tocan PERFIL_USUARIO ni SUSCRIPCION, donde la relación 1:1 con el usuario es
-- intencional, ni USUARIO (donde id_usuario es la PK).
--
-- Eliminamos cualquier restricción UNIQUE definida SOLO sobre id_usuario en esas tablas, de forma
-- defensiva (por definición, no por nombre autogenerado, y sin fallar si la tabla/constraint no existe).
DO
$$
    DECLARE
        c record;
    BEGIN
        FOR c IN
            SELECT rel.relname AS table_name, con.conname AS constraint_name
            FROM pg_constraint con
                     JOIN pg_class rel ON rel.oid = con.conrelid
                     JOIN pg_namespace nsp ON nsp.oid = rel.relnamespace
            WHERE con.contype = 'u'
              AND nsp.nspname = current_schema()
              AND rel.relname IN (
                                  'seguidores_artista',
                                  'historial_reproduccion',
                                  'comentarios',
                                  'calificaciones',
                                  'colaboracion_playlist'
                )
              AND (
                      SELECT array_agg(att.attname::text)
                      FROM unnest(con.conkey) AS k(attnum)
                               JOIN pg_attribute att
                                    ON att.attrelid = con.conrelid AND att.attnum = k.attnum
                  ) = ARRAY ['id_usuario']::text[]
            LOOP
                EXECUTE format('ALTER TABLE %I DROP CONSTRAINT %I', c.table_name, c.constraint_name);
            END LOOP;
    END
$$;
