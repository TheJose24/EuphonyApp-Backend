CREATE TABLE PREFERENCIAS_USUARIO (
    id_preferencia SERIAL PRIMARY KEY,
    id_usuario UUID NOT NULL,
    genero_favorito VARCHAR(50),
    calidad_audio VARCHAR(20), -- (alta, media, baja)
    tema_visual VARCHAR(20), -- (claro, oscuro)
    CONSTRAINT fk_preferencias_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario)
);
