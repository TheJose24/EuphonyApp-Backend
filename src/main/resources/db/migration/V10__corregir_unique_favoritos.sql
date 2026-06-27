-- Corrige un bug latente del esquema inicial (V1): las tablas puente FAVORITOS_CANCION y
-- FAVORITOS_ALBUM se crearon con `id_usuario UUID UNIQUE`, una restricción UNIQUE sobre la
-- columna sola. Eso limitaría a CADA usuario a un único favorito (una sola canción / un solo
-- álbum), rompiendo la funcionalidad de favoritos. La unicidad correcta ya la garantiza la
-- clave primaria compuesta (id_usuario, id_cancion) / (id_usuario, id_album).
--
-- Eliminamos cualquier restricción UNIQUE definida SOLO sobre id_usuario en esas dos tablas.
-- Se hace de forma defensiva (buscando el constraint por su definición, no por su nombre
-- autogenerado) para ser robustos ante distintos nombres por entorno.
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
              AND rel.relname IN ('favoritos_cancion', 'favoritos_album')
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
