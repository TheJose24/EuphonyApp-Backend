# Configuración y Estructura de la Base de Datos

## Índice
1. [Información General](#información-general)
2. [Configuración de la Base de Datos](#configuración-de-la-base-de-datos)
3. [Variables de Entorno](#variables-de-entorno)
4. [Estructura de las Tablas](#estructura-de-las-tablas)
5. [Relaciones entre Tablas](#relaciones-entre-tablas)
6. [Índices](#índices)

## Información General

Este documento proporciona información sobre la configuración y estructura de la base de datos para nuestra plataforma de streaming de música.

## Configuración de la Base de Datos

### Sistema de Gestión de Base de Datos
Utilizamos PostgreSQL como nuestro sistema de gestión de base de datos relacional.

## Variables de Entorno

Para proteger la información sensible, utilizamos variables de entorno en nuestro archivo `application.properties`. Es importante configurar estas variables en su entorno de desarrollo.

### Configuración en IntelliJ IDEA

Si está utilizando IntelliJ IDEA, siga estos pasos para configurar las variables de entorno:

1. Vaya a `Run` > `Edit Configurations`.
2. Seleccione su configuración de ejecución.
3. En la sección `Environment variables`, añada las siguientes variables:
    - `DB_URL`
    - `DB_USER`
    - `DB_PASSWORD`

Ejemplo de `application.properties`:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
```

**Nota**: Asegúrese de no compartir los valores reales de estas variables en alguna de las versiones del código.

## Estructura de las Tablas

A continuación se presentan algunas de las tablas principales con sus estructuras:

### USUARIO
```sql
CREATE TABLE USUARIO (
    id_usuario       SERIAL PRIMARY KEY,
    nombre           VARCHAR(100)        NOT NULL,
    apellido         VARCHAR(100)        NOT NULL,
    username         VARCHAR(50) UNIQUE  NOT NULL,
    email            VARCHAR(255) UNIQUE NOT NULL,
    contrasena       VARCHAR(255)        NOT NULL,
    fecha_nacimiento DATE                NOT NULL,
    pais             VARCHAR(100)        NOT NULL,
    img_perfil       VARCHAR(255),
    is_active        BOOLEAN   DEFAULT TRUE,
    last_login       TIMESTAMP,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### CANCION
```sql
CREATE TABLE CANCION (
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
```

### ARTISTA
```sql
CREATE TABLE ARTISTA
(
   id_artista     SERIAL PRIMARY KEY,
   nombre         VARCHAR(255) NOT NULL,
   biografia      TEXT,
   pais           VARCHAR(100) NOT NULL,
   redes_sociales JSON,
   is_verified    BOOLEAN DEFAULT FALSE
);
```

### ALBUM
```sql
CREATE TABLE ALBUM
(
   id_album          SERIAL PRIMARY KEY,
   id_artista        INTEGER,
   titulo            VARCHAR(255) NOT NULL,
   fecha_lanzamiento DATE         NOT NULL,
   portada           VARCHAR(255),
   CONSTRAINT fk_album_artista FOREIGN KEY (id_artista) REFERENCES ARTISTA (id_artista) ON DELETE CASCADE
);
```

### GENERO
```sql
CREATE TABLE GENERO
(
   id_genero   SERIAL PRIMARY KEY,
   nombre      VARCHAR(100) UNIQUE NOT NULL,
   descripcion TEXT
);
```

### SUSCRIPCION
```sql
CREATE TABLE SUSCRIPCION
(
   id_suscripcion    SERIAL PRIMARY KEY,
   id_usuario        INTEGER,
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
```

### FAVORITOS
```sql
CREATE TABLE FAVORITOS
(
   id_usuario     INTEGER,
   id_cancion     INTEGER,
   fecha_agregado TIMESTAMP NOT NULL,
   PRIMARY KEY (id_usuario, id_cancion),
   CONSTRAINT fk_favoritos_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario) ON DELETE CASCADE,
   CONSTRAINT fk_favoritos_cancion FOREIGN KEY (id_cancion) REFERENCES CANCION (id_cancion) ON DELETE CASCADE
);
```

### CANCION_GENERO
```sql
CREATE TABLE CANCION_GENERO
(
   id_cancion INTEGER,
   id_genero  INTEGER,
   PRIMARY KEY (id_cancion, id_genero),
   CONSTRAINT fk_cancion_genero_cancion FOREIGN KEY (id_cancion) REFERENCES CANCION (id_cancion) ON DELETE CASCADE,
   CONSTRAINT fk_cancion_genero_genero FOREIGN KEY (id_genero) REFERENCES GENERO (id_genero) ON DELETE CASCADE
);
```

## Relaciones entre Tablas

Ejemplos de relaciones clave:

- USUARIO -> SUSCRIPCION (One-to-Many)
- ARTISTA -> ALBUM (One-to-Many)
- ALBUM -> CANCION (One-to-Many)
- USUARIO <-> CANCION (Many-to-Many a través de FAVORITOS)
- CANCION <-> GENERO (Many-to-Many a través de CANCION_GENERO)

## Índices

Hemos creado índices en columnas frecuentemente consultadas para mejorar el rendimiento:

```sql
CREATE INDEX idx_username ON USUARIO (username, email);
CREATE INDEX idx_album_titulo ON ALBUM (titulo);
CREATE INDEX idx_cancion_titulo ON CANCION (titulo);
CREATE INDEX idx_playlist_cancion ON PLAYLIST_CANCION (id_playlist, id_cancion);
```

Para más detalles sobre las tablas y relaciones, consulte el diagrama de la base de datos en el archivo `db_diagram.png`.