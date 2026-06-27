-- Corrige un bug latente del esquema inicial (V1): la tabla PLAYLIST se creó con
-- `id_usuario UUID UNIQUE`, una restricción UNIQUE sobre la columna sola. Eso limitaría a CADA
-- usuario a tener UNA sola playlist, lo cual contradice la relación @ManyToOne (muchas playlists
-- por usuario), al endpoint GET /api/v1/playlists/user/{userId} (que devuelve una lista) y a la
-- propia lógica de creación (que ya valida nombres duplicados entre las playlists de un usuario).
--
-- Eliminamos cualquier restricción UNIQUE definida SOLO sobre id_usuario en la tabla PLAYLIST,
-- de forma defensiva (buscando el constraint por su definición, no por su nombre autogenerado).
-- La clave foránea a USUARIO y el resto de columnas se conservan intactas.
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
              AND rel.relname = 'playlist'
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
