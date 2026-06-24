ALTER TABLE PLANES_SUSCRIPCION
    ADD COLUMN intervalo VARCHAR(150),
    ADD COLUMN funcionalidades JSONB,
    ADD COLUMN tarifa_inicial NUMERIC(10,2),
    ADD COLUMN free_trial BOOLEAN,
    ADD COLUMN dias_prueba INTEGER;

CREATE INDEX idx_funcionalidades_jsonb ON PLANES_SUSCRIPCION USING GIN (funcionalidades);

ALTER TABLE SUSCRIPCION
    ADD COLUMN fecha_ultima_actualizacion TIMESTAMP;

