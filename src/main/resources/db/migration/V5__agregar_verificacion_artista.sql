-- 1. Agregar campos de verificación a la tabla ARTISTA
ALTER TABLE ARTISTA
    ADD COLUMN verification_date TIMESTAMP DEFAULT NULL,
    ADD COLUMN verification_reason TEXT DEFAULT '';
