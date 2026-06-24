CREATE TABLE ROLES
(
    id_rol      SERIAL PRIMARY KEY,
    nombre_rol  VARCHAR(50) UNIQUE NOT NULL
);

CREATE INDEX idx_roles_nombre ON ROLES (nombre_rol);

-- Create USUARIO table
CREATE TABLE USUARIO
(
    id_usuario       UUID UNIQUE PRIMARY KEY,
    username         VARCHAR(50) UNIQUE  NOT NULL,
    email            VARCHAR(255) UNIQUE NOT NULL,
    nombre           VARCHAR(100)        NOT NULL,
    apellido         VARCHAR(100)        NOT NULL,
    is_active        BOOLEAN   DEFAULT TRUE,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_username ON USUARIO (username, email);

-- Create USUARIO_ROLES table
CREATE TABLE USUARIO_ROLES
(
    id_usuario  UUID NOT NULL,
    id_rol      INTEGER NOT NULL,
    PRIMARY KEY (id_usuario, id_rol),
    CONSTRAINT fk_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_rol FOREIGN KEY (id_rol) REFERENCES ROLES (id_rol) ON DELETE CASCADE
);

CREATE INDEX idx_usuario_roles ON USUARIO_ROLES(id_usuario, id_rol);

CREATE TABLE PERFIL_USUARIO
(
    id_perfil SERIAL PRIMARY KEY,
    id_usuario     UUID UNIQUE,
    fecha_nacimiento DATE,
    pais             VARCHAR(100),
    img_perfil       VARCHAR(255),
    telefono       VARCHAR(20),
    ciudad         VARCHAR(100),
    CONSTRAINT fk_perfil_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario) ON DELETE CASCADE
);

-- Create PLANES_SUSCRIPCION table
CREATE TABLE PLANES_SUSCRIPCION
(
    id_plan     SERIAL PRIMARY KEY,
    nombre_plan VARCHAR(100) UNIQUE NOT NULL,
    precio      DECIMAL(10, 2)      NOT NULL,
    duracion    INTEGER             NOT NULL CHECK (duracion > 0),
    descripcion TEXT,
    is_active   BOOLEAN DEFAULT TRUE
);

-- Create METODO_PAGO table
CREATE TABLE METODO_PAGO
(
    id_metodo_pago SERIAL PRIMARY KEY,
    tipo           VARCHAR(50) NOT NULL,
    detalles       TEXT,
    is_active      BOOLEAN DEFAULT TRUE
);

-- Create SUSCRIPCION table
CREATE TABLE SUSCRIPCION
(
    id_suscripcion    SERIAL PRIMARY KEY,
    id_usuario        UUID UNIQUE,
    id_plan           INTEGER,
    id_metodo_pago    INTEGER,
    fecha_inicio      DATE NOT NULL,
    fecha_renovacion  DATE NOT NULL,
    fecha_cancelacion DATE,
    estado            VARCHAR(10) DEFAULT 'activa' CHECK (estado IN ('activa', 'cancelada', 'expirada')),
    created_at        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_suscripcion_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_suscripcion_plan FOREIGN KEY (id_plan) REFERENCES PLANES_SUSCRIPCION (id_plan) ON DELETE CASCADE,
    CONSTRAINT fk_suscripcion_metodo_pago FOREIGN KEY (id_metodo_pago) REFERENCES METODO_PAGO (id_metodo_pago) ON DELETE CASCADE
);

-- Create ARTISTA table
CREATE TABLE ARTISTA
(
    id_artista     SERIAL PRIMARY KEY,
    nombre         VARCHAR(255) NOT NULL,
    biografia      TEXT,
    pais           VARCHAR(100) NOT NULL,
    redes_sociales JSON,
    is_verified    BOOLEAN DEFAULT FALSE
);

-- Create ALBUM table
CREATE TABLE ALBUM
(
    id_album          SERIAL PRIMARY KEY,
    id_artista        INTEGER,
    titulo            VARCHAR(255) NOT NULL,
    fecha_lanzamiento DATE         NOT NULL,
    portada           VARCHAR(255),
    CONSTRAINT fk_album_artista FOREIGN KEY (id_artista) REFERENCES ARTISTA (id_artista) ON DELETE CASCADE
);

CREATE INDEX idx_album_titulo ON ALBUM (titulo);

-- Create GENERO table
CREATE TABLE GENERO
(
    id_genero   SERIAL PRIMARY KEY,
    nombre      VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT
);

-- Create CANCION table
CREATE TABLE CANCION
(
    id_cancion            SERIAL PRIMARY KEY,
    id_artista            INTEGER,
    id_album              INTEGER,
    titulo                VARCHAR(255)        NOT NULL,
    portada               VARCHAR(255),
    duracion              INTERVAL            NOT NULL,
    idioma                VARCHAR(50)         NOT NULL,
    letra                 TEXT,
    fecha_lanzamiento     DATE                NOT NULL,
    ruta_archivo          VARCHAR(255) UNIQUE NOT NULL,
    calificacion_promedio DECIMAL(3, 2),
    numero_reproducciones INTEGER DEFAULT 0,
    CONSTRAINT fk_cancion_artista FOREIGN KEY (id_artista) REFERENCES ARTISTA (id_artista) ON DELETE CASCADE,
    CONSTRAINT fk_cancion_album FOREIGN KEY (id_album) REFERENCES ALBUM (id_album) ON DELETE CASCADE
);

CREATE INDEX idx_cancion_titulo ON CANCION (titulo);

-- Create CANCION_GENERO table (Many-to-Many relationship)
CREATE TABLE CANCION_GENERO
(
    id_cancion INTEGER,
    id_genero  INTEGER,
    PRIMARY KEY (id_cancion, id_genero),
    CONSTRAINT fk_cancion_genero_cancion FOREIGN KEY (id_cancion) REFERENCES CANCION (id_cancion) ON DELETE CASCADE,
    CONSTRAINT fk_cancion_genero_genero FOREIGN KEY (id_genero) REFERENCES GENERO (id_genero) ON DELETE CASCADE
);

CREATE INDEX idx_cancion_genero ON CANCION_GENERO (id_cancion, id_genero);

-- Create PLAYLIST table
CREATE TABLE PLAYLIST
(
    id_playlist    SERIAL PRIMARY KEY,
    id_usuario     UUID UNIQUE,
    nombre         VARCHAR(255) NOT NULL,
    fecha_creacion DATE         NOT NULL,
    descripcion    TEXT,
    is_public      BOOLEAN DEFAULT TRUE,
    img_portada    VARCHAR(255),
    CONSTRAINT fk_playlist_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario) ON DELETE CASCADE
);

-- Create PLAYLIST_CANCION table (Many-to-Many relationship)
CREATE TABLE PLAYLIST_CANCION
(
    id_playlist INTEGER,
    id_cancion  INTEGER,
    PRIMARY KEY (id_playlist, id_cancion),
    CONSTRAINT fk_playlist_cancion_playlist FOREIGN KEY (id_playlist) REFERENCES PLAYLIST (id_playlist) ON DELETE CASCADE,
    CONSTRAINT fk_playlist_cancion_cancion FOREIGN KEY (id_cancion) REFERENCES CANCION (id_cancion) ON DELETE CASCADE
);

CREATE INDEX idx_playlist_cancion ON PLAYLIST_CANCION (id_playlist, id_cancion);

-- Create HISTORIAL_REPRODUCCION table
CREATE TABLE HISTORIAL_REPRODUCCION
(
    id_historial       SERIAL PRIMARY KEY,
    id_usuario         UUID UNIQUE,
    id_cancion         INTEGER,
    fecha_reproduccion TIMESTAMP NOT NULL,
    CONSTRAINT fk_historial_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_historial_cancion FOREIGN KEY (id_cancion) REFERENCES CANCION (id_cancion) ON DELETE CASCADE
);

-- Create FAVORITOS table
CREATE TABLE FAVORITOS_CANCION
(
    id_usuario     UUID UNIQUE,
    id_cancion     INTEGER,
    fecha_agregado TIMESTAMP NOT NULL,
    PRIMARY KEY (id_usuario, id_cancion),
    CONSTRAINT fk_favoritos_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_favoritos_cancion FOREIGN KEY (id_cancion) REFERENCES CANCION (id_cancion) ON DELETE CASCADE
);

CREATE TABLE FAVORITOS_ALBUM
(
    id_usuario     UUID UNIQUE,
    id_album       INTEGER,
    fecha_agregado TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario, id_album),
    CONSTRAINT fk_favoritos_album_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_favoritos_album_album FOREIGN KEY (id_album) REFERENCES ALBUM (id_album) ON DELETE CASCADE
);

-- Create COMENTARIOS table
CREATE TABLE COMENTARIOS
(
    id_comentario    SERIAL PRIMARY KEY,
    id_usuario       UUID UNIQUE,
    id_cancion       INTEGER,
    comentario       TEXT      NOT NULL,
    fecha_comentario TIMESTAMP NOT NULL,
    CONSTRAINT fk_comentarios_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_comentarios_cancion FOREIGN KEY (id_cancion) REFERENCES CANCION (id_cancion) ON DELETE CASCADE
);

-- Create CALIFICACIONES table
CREATE TABLE CALIFICACIONES
(
    id_usuario         UUID UNIQUE,
    id_cancion         INTEGER,
    calificacion       INTEGER CHECK (calificacion BETWEEN 1 AND 5),
    fecha_calificacion TIMESTAMP NOT NULL,
    PRIMARY KEY (id_usuario, id_cancion),
    CONSTRAINT fk_calificaciones_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_calificaciones_cancion FOREIGN KEY (id_cancion) REFERENCES CANCION (id_cancion) ON DELETE CASCADE
);

-- Create NOTIFICACIONES table
CREATE TABLE NOTIFICACIONES
(
    id_notificacion SERIAL PRIMARY KEY,
    id_usuario      UUID UNIQUE,
    mensaje         TEXT      NOT NULL,
    leido           BOOLEAN DEFAULT FALSE,
    fecha_envio     TIMESTAMP NOT NULL,
    CONSTRAINT fk_notificaciones_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario) ON DELETE CASCADE
);

-- Create SEGUIDORES_ARTISTA table
CREATE TABLE SEGUIDORES_ARTISTA
(
    id_usuario        UUID UNIQUE,
    id_artista        INTEGER,
    fecha_seguimiento TIMESTAMP NOT NULL,
    PRIMARY KEY (id_usuario, id_artista),
    CONSTRAINT fk_seguidores_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_seguidores_artista FOREIGN KEY (id_artista) REFERENCES ARTISTA (id_artista) ON DELETE CASCADE
);

-- Create COLABORACION_PLAYLIST table
CREATE TABLE COLABORACION_PLAYLIST
(
    id_playlist        INTEGER,
    id_usuario         UUID UNIQUE,
    fecha_colaboracion TIMESTAMP NOT NULL,
    PRIMARY KEY (id_playlist, id_usuario),
    CONSTRAINT fk_colaboracion_playlist FOREIGN KEY (id_playlist) REFERENCES PLAYLIST (id_playlist) ON DELETE CASCADE,
    CONSTRAINT fk_colaboracion_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario) ON DELETE CASCADE
);