# Euphony API — Documentación de Endpoints

> Documentación de referencia para implementar las llamadas al backend desde el frontend.
> Generada a partir del código fuente (controladores + DTOs) del proyecto **Euphony Streaming**.

API RESTful para una plataforma de streaming de música: gestión de usuarios, perfiles,
artistas, álbumes, canciones, géneros, playlists, seguidores, planes y suscripciones (PayPal).

---

## 1. Información general

| Dato | Valor |
|------|-------|
| **Base URL (desarrollo)** | `http://localhost:8080` |
| **Base URL (producción)** | `https://euphony-api.devbyjose.org` |
| **Prefijo común de la API** | `/api/v1` |
| **Formato de datos** | JSON (`application/json`) salvo endpoints de archivos (`multipart/form-data`) |
| **Documentación interactiva (Swagger UI)** | `http://localhost:8080/swagger-ui.html` |
| **OpenAPI (JSON crudo)** | `http://localhost:8080/api-docs` |
| **Versión API** | `1.0.0` |

> ⚠️ **No hay `context-path` configurado.** Las rutas son exactamente las indicadas en cada sección
> (ej. `http://localhost:8080/api/v1/users/all`).

### Convenciones de rutas observadas en el código (importante)

El backend **no usa un patrón uniforme** entre módulos. Respeta exactamente lo siguiente:

- La mayoría de módulos usan sufijos explícitos: `/all`, `/create`, `/update/{id}`, `/delete/{id}`, `/search/...`.
- **Playlists es la excepción**: usa rutas REST puras sobre la raíz `/api/v1/playlists` (sin `/all`, `/create`, etc.).

---

## 2. Autenticación

El backend está configurado como **OAuth2 Resource Server con JWT** (Keycloak, realm `euphony-client`).
El esquema de seguridad documentado es **Bearer Token**:

```http
Authorization: Bearer <JWT>
```

> 🔓 **Estado actual del código:** en `SecurityConfig` la regla activa es `anyRequest().permitAll()`,
> por lo que **todos los endpoints de `/api/v1/**` son accesibles actualmente sin token**.
> El frontend debería estar preparado para **enviar el header `Authorization: Bearer`** cuando la
> seguridad se active (descomentando/cambiando esa línea), pero hoy no es obligatorio.
>
> La única zona protegida es `/management/**` (Actuator/Prometheus), que requiere **HTTP Basic**
> con rol `METRICS_ADMIN`. No es relevante para el frontend de la app.

- Los IDs de usuario son **UUID** (string), los demás IDs de entidades son **numéricos (Long)**.

### CORS

CORS está **habilitado** de forma centralizada en la cadena de seguridad (no es necesario proxy
para llamar a la API desde otro origen). Configuración aplicada a **todas** las rutas (`/**`):

| Aspecto | Valor |
|---------|-------|
| **Orígenes permitidos** | Propiedad `app.cors.allowed-origins` (lista separada por comas). Por defecto: `http://localhost:4200`, `http://127.0.0.1:4200`, `https://euphony.devbyjose.org` |
| **Métodos** | `GET, POST, PUT, PATCH, DELETE, OPTIONS` |
| **Headers de petición** | `*` (todos) |
| **Headers expuestos** | `Content-Disposition`, `Content-Range`, `Accept-Ranges`, `Content-Length` (para streaming de audio con `Range`) |
| **Credenciales** | `Access-Control-Allow-Credentials: true` |
| **Preflight** | `OPTIONS` responde `200/204` con las cabeceras CORS; cacheado `Max-Age: 3600` |

- Los orígenes son **configurables sin recompilar**: editar `app.cors.allowed-origins` o exportar la
  variable de entorno `APP_CORS_ALLOWED_ORIGINS` (lista separada por comas).
- Un origen **no listado** se rechaza (la respuesta no incluye `Access-Control-Allow-Origin`).
- Como `allowCredentials` es `true`, **no** se usa comodín `*` en orígenes (lista explícita).

> ⚠️ Ajustar el **dominio de producción** de `app.cors.allowed-origins` al dominio real donde se
> sirve el frontend.

---

## 3. Manejo de errores

### 3.1. Errores de validación de campos (`400 Bad Request`)
Cuando falla la validación de un DTO (`@NotNull`, `@Size`, `@Email`, etc.), la respuesta es un
**objeto JSON `{ campo: mensaje }`**:

```json
{
  "username": "El nombre de usuario debe tener entre 3 y 20 caracteres",
  "email": "Debe proporcionar un correo electrónico válido"
}
```

### 3.2. Errores de negocio / dominio
El resto de excepciones (no encontrado, conflicto, creación fallida, etc.) devuelven el
**mensaje como texto plano** (string en el body) con el código HTTP correspondiente:

```
Status: 404 Not Found
Body:   "El álbum con ID 99 no fue encontrado"
```

### 3.3. Error genérico (`500 Internal Server Error`)
```
"Error interno del servidor. Por favor, intente más tarde."
```

> 💡 **Recomendación frontend:** al parsear errores, intentar primero `JSON` (errores de validación)
> y si falla, tratar el body como `string` (errores de negocio).

---

## 4. Tipos de datos / formatos

| Tipo en el backend | Representación JSON | Ejemplo |
|--------------------|---------------------|---------|
| `UUID` | string | `"123e4567-e89b-12d3-a456-426614174000"` |
| `Long` / `Integer` | number | `1` |
| `BigDecimal` | number | `19.99` |
| `LocalDate` | string `yyyy-MM-dd` | `"1973-03-01"` |
| `LocalDateTime` | string ISO-8601 | `"2024-03-20T15:30:00"` |
| `Boolean` | boolean | `true` |
| `Set<String>` | array de strings | `["Pop", "Rock"]` |
| `Map<String,String>` | objeto | `{"Twitter": "@johndoe"}` |

Zona horaria del servidor: `America/Lima`.

---

## 5. Enumeraciones

### `PlanInterval`
`DAY` · `WEEK` · `MONTH` · `YEAR`

### `SubscriptionState`
`ACTIVE` · `CANCELED` · `SUSPENDED` · `EXPIRED` · `PENDIG` *(nota: `PENDIG` está así escrito en el código, sin la "N")*

### `SubscriptionFeature`
`PREMIUM_ACCOUNT` · `TWO_ACCOUNTS` · `FOUR_ACCOUNTS` · `CANCEL_ANYTIME` · `HIGH_QUALITY` · `OFFLINE_MODE` · `NO_ADS` · `LYRICS` · `EXCLUSIVE_CONTENT` · `CUSTOM_PLAYLISTS`

### `PaymentType`
`TRIAL` · `REGULAR`

---

# 6. Endpoints

Resumen de módulos:

| Módulo | Base path | Sección |
|--------|-----------|---------|
| Usuarios | `/api/v1/users` | [6.1](#61-usuarios) |
| Perfil de usuario | `/api/v1/users/profile` | [6.2](#62-perfil-de-usuario) |
| Artistas | `/api/v1/artists` | [6.3](#63-artistas) |
| Seguidores | `/api/v1/followers` | [6.4](#64-seguidores) |
| Álbumes | `/api/v1/albums` | [6.5](#65-álbumes) |
| Canciones | `/api/v1/songs` | [6.6](#66-canciones) |
| Géneros | `/api/v1/genres` | [6.7](#67-géneros) |
| Playlists | `/api/v1/playlists` | [6.8](#68-playlists) |
| Planes de suscripción | `/api/v1/plans` | [6.9](#69-planes-de-suscripción) |
| Suscripciones | `/api/v1/subscriptions` | [6.10](#610-suscripciones) |
| Favoritos (likes) | `/api/v1/favorites` | [6.11](#611-favoritos) |
| Archivos / imágenes | `/uploads/...` | [6.12](#612-archivos--imágenes) |

---

## 6.1. Usuarios
**Base:** `/api/v1/users`

| Método | Ruta | Descripción | Body | Respuesta |
|--------|------|-------------|------|-----------|
| GET | `/all` | Lista todos los usuarios | — | `200` → `UserResponseDTO[]` |
| GET | `/search/{id}` | Usuario por ID (UUID) | — | `200` → `UserResponseDTO` · `404` |
| POST | `/create` | Crea un usuario | `UserRequestDTO` | `201` → `UUID` (string) · `409` · `500` |
| PUT | `/update/{id}` | Actualiza un usuario (UUID) | `UserRequestDTO` | `204` · `404` · `500` |
| DELETE | `/delete/{id}` | Elimina un usuario (UUID) | — | `204` · `404` · `500` |

**`UserRequestDTO` (request):**
```json
{
  "username": "johndoe123",          // requerido, 3-20 caracteres
  "email": "john.doe@example.com",   // requerido, email válido
  "password": "password123",         // requerido, mínimo 8 caracteres
  "firstName": "John",               // requerido
  "lastName": "Doe",                 // requerido
  "roles": ["user_client_role", "admin_client_role", "artist_client_role"]  // opcional
}
```

**`UserResponseDTO` (response):**
```json
{
  "idUsuario": "123e4567-e89b-12d3-a456-556642440000",
  "email": "john.doe@example.com",
  "username": "johndoe123",
  "firstName": "John",
  "lastName": "Doe",
  "isActive": true,
  "roles": [ /* objetos de rol */ ]
}
```

> ℹ️ `POST /create` devuelve directamente el **UUID del usuario creado** (string JSON entre comillas), no un objeto.

---

## 6.2. Perfil de usuario
**Base:** `/api/v1/users/profile`

| Método | Ruta | Descripción | Body | Respuesta |
|--------|------|-------------|------|-----------|
| GET | `/all` | Lista todos los perfiles | — | `200` → `UserProfileResponseDTO[]` |
| GET | `/search/{userId}` | Perfil por ID de usuario (UUID) | — | `200` → `UserProfileResponseDTO` · `404` |
| PUT | `/update/{userId}` | Actualiza el perfil (UUID) | `UserProfileRequestDTO` | `204` |
| DELETE | `/delete/{userId}` | Elimina el perfil (UUID) | — | `204` |
| GET | `/profile-image/{userId}` | Devuelve la ruta/URL de la imagen de perfil | — | `200` → `String` |

**`UserProfileRequestDTO` (request):**
```json
{
  "birthDate": "1990-01-01",          // fecha (Date)
  "country": "Perú",
  "imgProfile": "/imagenes/usuario.jpg",
  "phone": "+51 987654321",
  "city": "Lima"
}
```

**`UserProfileResponseDTO` (response):**
```json
{
  "idProfile": 1,
  "idUser": "550e8400-e29b-41d4-a716-446655440000",
  "username": "juanperez123",
  "email": "juan.perez@example.com",
  "firstName": "Juan",
  "lastName": "Perez",
  "birthDate": "1990-05-20",
  "imgProfile": "/imagenes/usuario.jpg",
  "phone": "+51 987654321",
  "country": "Perú",
  "city": "Lima"
}
```

---

## 6.3. Artistas
**Base:** `/api/v1/artists`

| Método | Ruta | Descripción | Body | Respuesta |
|--------|------|-------------|------|-----------|
| GET | `/all` | Lista todos los artistas | — | `200` → `ArtistResponseDTO[]` |
| GET | `/search/{name}` | Busca artista por **nombre** | — | `200` → `ArtistResponseDTO` · `404` · `400` |
| POST | `/create` | Crea un artista (**multipart**) | `multipart/form-data` | `201` · `409` · `400` |
| PUT | `/update/{id}` | Actualiza un artista (**multipart**, Long) | `multipart/form-data` | `200` · `404` · `400` |
| DELETE | `/delete/{id}` | Elimina un artista (Long) | — | `204` · `404` |

### `POST /create` y `PUT /update/{id}` — `multipart/form-data`
Dos partes (mismo patrón que **Álbumes**):
- **`artistRequestDTO`** → parte JSON con el cuerpo `ArtistRequestDTO` (Content-Type `application/json`).
- **`imageFile`** → archivo de imagen **opcional** del artista.

En `update`, si **no** se envía `imageFile`, se conserva la imagen actual (no se borra).

**`ArtistRequestDTO` (parte JSON):**
```json
{
  "name": "John Doe",                 // requerido, no vacío
  "biography": "Texto biográfico...", // opcional
  "country": "USA",                   // requerido, no vacío
  "socialNetworks": {                 // opcional, mapa clave/valor
    "Twitter": "@johndoe",
    "Instagram": "johndoe"
  }
}
```

**Ejemplo de construcción del FormData (frontend):**
```js
const fd = new FormData();
fd.append("artistRequestDTO", new Blob([JSON.stringify(artist)], { type: "application/json" }));
if (file) fd.append("imageFile", file);   // opcional
// fetch(..., { method: "POST", body: fd })  -> NO fijar Content-Type manualmente
```

**`ArtistResponseDTO` (response):**
```json
{
  "idArtist": 1,
  "name": "John Doe",
  "biography": "Texto biográfico...",
  "country": "USA",
  "imageUrl": "/uploads/images/artist_1_abc123.jpg",  // puede ser null (artista sin imagen)
  "socialNetworks": { "Twitter": "@johndoe", "Instagram": "johndoe" },
  "isVerified": false
}
```

> ℹ️ `imageUrl` es **nullable**: es `null` cuando el artista no tiene imagen (el frontend cae al avatar con inicial).
> La ruta es relativa y se sirve por `/uploads/images/...` (igual que `portada` de los álbumes); anteponer la Base URL.
> Este campo aparece en **todos** los endpoints que devuelven `ArtistResponseDTO`, incluido el `artista` anidado dentro de `AlbumResponseDTO`.

---

## 6.4. Seguidores
**Base:** `/api/v1/followers`

| Método | Ruta | Descripción | Body | Respuesta |
|--------|------|-------------|------|-----------|
| POST | `/follow` | Un usuario sigue a un artista | `FollowersArtistRequestDTO` | `201` (sin body) · `400` · `404` |
| DELETE | `/unfollow` | Un usuario deja de seguir a un artista | `FollowersArtistRequestDTO` | `200` (sin body) · `400` · `404` |
| GET | `/by-artist/{artistId}` | Seguidores de un artista (Long) | — | `200` → `FollowersArtistResponseDTO[]` · `404` |
| GET | `/by-user/{userId}` | Artistas seguidos por un usuario (UUID) | — | `200` → `FollowersArtistResponseDTO[]` · `404` |

**`FollowersArtistRequestDTO` (request):**
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",  // requerido (UUID)
  "artistId": 1                                        // requerido (Long)
}
```

**`FollowersArtistResponseDTO` (response):**
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "userName": "John Doe",
  "artistId": 1,
  "artistName": "Taylor Swift",
  "followDate": "2024-03-20T15:30:00"
}
```

> ⚠️ `POST /follow` y `DELETE /unfollow` **no devuelven body** (a pesar de declarar el tipo). Usar solo el status code.
> Nota: `DELETE /unfollow` lleva **body JSON** en la petición (poco habitual para un DELETE, pero así está implementado).

---

## 6.5. Álbumes
**Base:** `/api/v1/albums`

| Método | Ruta | Descripción | Body | Respuesta |
|--------|------|-------------|------|-----------|
| GET | `/all` | Lista todos los álbumes | — | `200` → `AlbumResponseDTO[]` |
| GET | `/search/by-name/{name}` | Busca álbum por nombre | — | `200` → `AlbumResponseDTO` · `404` |
| GET | `/search/by-artist/{artist}` | Álbumes de un artista (por nombre) | — | `200` → `AlbumResponseDTO[]` · `404` |
| GET | `/search/by-id/{id}` | Álbum por ID (Long) | — | `200` → `AlbumResponseDTO` · `404` |
| POST | `/create` | Crea un álbum (**multipart**) | `multipart/form-data` | `201` (texto) · `400` · `409` |
| PUT | `/update/{id}` | Actualiza un álbum (**multipart**) | `multipart/form-data` | `200` (texto) · `400` · `404` · `409` |
| DELETE | `/delete/{id}` | Elimina un álbum (Long) | — | `204` (texto) · `404` · `500` |

### `POST /create` y `PUT /update/{id}` — `multipart/form-data`
Dos partes:
- **`albumRequestDTO`** → parte JSON con el cuerpo `AlbumRequestDTO` (Content-Type `application/json`).
- **`coverImage`** → archivo de imagen **opcional**.

**`AlbumRequestDTO` (parte JSON):**
```json
{
  "idArtist": 1,                       // requerido (Long)
  "title": "The Dark Side of the Moon", // requerido
  "releaseDate": "1973-03-01",          // opcional, formato yyyy-MM-dd
  "cover": "https://.../cover.png"      // opcional (URL); alternativa a subir coverImage
}
```

**Ejemplo de construcción del FormData (frontend):**
```js
const fd = new FormData();
fd.append("albumRequestDTO", new Blob([JSON.stringify(album)], { type: "application/json" }));
if (file) fd.append("coverImage", file);   // opcional
// fetch(..., { method: "POST", body: fd })  -> NO fijar Content-Type manualmente
```

**`AlbumResponseDTO` (response):**
```json
{
  "idAlbum": 1,
  "artista": { /* ArtistResponseDTO */ },
  "titulo": "The Dark Side of the Moon",
  "fechaLanzamiento": "1973-03-01",
  "portada": "https://.../cover.png"
}
```

> ℹ️ `create`/`update`/`delete` devuelven un **mensaje de texto plano** (ej. `"Álbum creado exitosamente"`), no JSON.

---

## 6.6. Canciones
**Base:** `/api/v1/songs`

| Método | Ruta | Descripción | Body | Respuesta |
|--------|------|-------------|------|-----------|
| GET | `/all` | Lista todas las canciones | — | `200` → `SongResponseDTO[]` · `204` (lista vacía) |
| GET | `/search/{id}` | Canción por ID (Long) | — | `200` → `SongResponseDTO` · `404` |
| GET | `/search/by-album/{albumId}` | Canciones de un álbum (Long) | — | `200` → `SongResponseDTO[]` · `404` |
| GET | `/search/by-artist/{artistId}` | Canciones de un artista (Long) | — | `200` → `SongResponseDTO[]` · `404` |
| POST | `/analyze` | Analiza metadatos de un archivo de audio (**multipart**) | `songFile` | `200` → `SongMetadataResponseDTO` · `400` |
| POST | `/create` | Crea una canción (**multipart**) | `songFile` + `songRequest` | `201` (sin body) · `400` |
| PUT | `/update/{id}` | Actualiza una canción (**multipart**) | `coverArt` (opcional) + `songRequest` | `200` · `400` · `404` |
| DELETE | `/delete/{id}` | Elimina una canción (Long) | — | `204` · `404` |
| GET | `/stream/{id}` | Reproduce/transmite la canción (streaming con soporte `Range`) | — | `200` / `206` (audio) · `404` |

### `GET /search/by-album/{albumId}` y `GET /search/by-artist/{artistId}`
- Devuelven la lista de canciones (`SongResponseDTO[]`) pertenecientes a un álbum o a un artista, respectivamente,
  cada una con su artista, álbum y géneros resueltos.
- `albumId` y `artistId` son **Long** (numéricos). `404` si el álbum/artista no existe.
- Ejemplos: `GET /api/v1/songs/search/by-album/2`, `GET /api/v1/songs/search/by-artist/1`.

### `POST /analyze` — `multipart/form-data`
- **`songFile`** → archivo de audio (`@RequestParam`, **requerido**). Devuelve los metadatos extraídos sin
  guardar nada. Útil para precargar el formulario de creación. Un archivo vacío es rechazado (error del servidor).

### `POST /create` — `multipart/form-data`
- **`songFile`** → archivo de audio (`@RequestParam`, **requerido**, no puede estar vacío).
- **`songRequest`** → parte JSON con `SongRequestDTO` (`@RequestPart`, **requerido**).
- Respuesta `201 Created` **sin body**.

### `PUT /update/{id}` — `multipart/form-data`
- **`coverArt`** → archivo de imagen de portada (`@RequestParam(required = false)`, **opcional**):
  si no se envía o va vacío, se conserva la portada actual.
- **`songRequest`** → parte JSON con `SongRequestDTO` (`@RequestPart`, requerido).

**`SongRequestDTO` (parte JSON):**
```json
{
  "title": "Mi Canción Favorita",
  "artist": "Artista",
  "album": "Mi Album",
  "releaseDate": "2023",
  "language": "Español",
  "duration": "00:03:45",            // formato HH:mm:ss
  "genres": ["Pop", "Rock"],
  "lyrics": "Letra de la canción...",
  "albumCoverPath": "/uploads/images/default_cover_art.png"
}
```

**`SongMetadataResponseDTO` (response de `/analyze`):**
```json
{
  "title": "Mi Canción Favorita",
  "artist": "Artista",
  "album": "Mi Album",
  "releaseDate": "2023",
  "duration": "00:03:45",
  "genres": ["Pop", "Rock"],
  "lyrics": "Letra de la canción...",
  "filePath": "/uploads/audio/cancion.mp3",
  "albumCoverPath": "/uploads/images/default_cover_art.png"
}
```

**`SongResponseDTO` (response):** *(DTO enriquecido — incluye nombre de artista, título y portada del álbum y géneros)*
```json
{
  "songId": 10,
  "artistId": 1,
  "artistName": "Taylor Swift",      // enriquecido (nombre del artista)
  "albumId": 2,                      // puede ser null
  "albumTitle": "1989",              // enriquecido (título del álbum; null si no tiene álbum)
  "albumCover": "/uploads/images/album_1989.jpg", // enriquecido (portada del álbum; null si no tiene álbum)
  "title": "Mi Cancion Favorita",
  "coverImg": "/imagenes/portada.jpg",
  "duration": "00:03:45",
  "language": "Español",
  "lyrics": "Letra de la canción...",
  "releaseDate": "2023-11-01",
  "filePath": "/uploads/audio/cancion.mp3",
  "averageRating": 4.5,
  "numberOfPlays": 1000,
  "genres": ["Pop", "Rock"]          // Set<String>, orden alfabético determinista
}
```

> ℹ️ Los campos `artistName`, `albumTitle`, `albumCover` y `genres` vienen resueltos por el backend (sin
> llamadas extra). Este mismo `SongResponseDTO` enriquecido se devuelve en **todos** los listados de canciones:
> `/all`, `/search/{id}`, `/search/by-album/{albumId}`, `/search/by-artist/{artistId}`, las canciones favoritas
> (`/api/v1/favorites/songs/by-user/{userId}`) y las canciones de una playlist (`/api/v1/playlists/{id}/songs`).

### `GET /stream/{id}` — Reproducción de audio
- Devuelve audio (`audio/mpeg`) en streaming.
- Soporta peticiones parciales mediante el header **`Range: bytes=0-`** → responde `206 Partial Content`
  con `Accept-Ranges`, `Content-Range` y `Content-Length`. Sin `Range` responde `200 OK` con el archivo completo.
- En el frontend basta con apuntar el `src` de un `<audio>`/reproductor a esta URL:
  `http://localhost:8080/api/v1/songs/stream/10`

---

## 6.7. Géneros
**Base:** `/api/v1/genres`

| Método | Ruta | Descripción | Body | Respuesta |
|--------|------|-------------|------|-----------|
| GET | `/all` | Lista todos los géneros | — | `200` → `GenreResponseDTO[]` |
| GET | `/search/{name}` | Género por nombre | — | `200` → `GenreResponseDTO` · `404` |
| POST | `/create` | Crea un género | `GenreRequestDTO` | `201` · `409` |
| PUT | `/update/{id}` | Actualiza un género (Long) | `GenreRequestDTO` | `200` · `404` |
| DELETE | `/delete/{id}` | Elimina un género (Long) | — | `204` · `404` |

**`GenreRequestDTO` (request):**
```json
{
  "name": "Rock",
  "description": "Género musical que se caracteriza por su ritmo y melodía"
}
```

**`GenreResponseDTO` (response):**
```json
{
  "idGenre": 1,
  "name": "Rock",
  "description": "Género musical que se caracteriza por su ritmo y melodía"
}
```

---

## 6.8. Playlists
**Base:** `/api/v1/playlists`

> ℹ️ Corrección: este módulo **no** usa REST puro sobre la raíz; usa los mismos sufijos que el resto
> (`/all`, `/search/{id}`, `/create`, `/update/{id}`, `/delete/{id}`).

**CRUD de la playlist:**

| Método | Ruta | Descripción | Body | Respuesta |
|--------|------|-------------|------|-----------|
| GET | `/all` | Lista todas las playlists | — | `200` → `PlaylistResponseDTO[]` |
| GET | `/search/{id}` | Playlist por ID (Long) | — | `200` → `PlaylistResponseDTO` · `404` |
| GET | `/user/{userId}` | Playlists de un usuario (UUID) | — | `200` → `PlaylistResponseDTO[]` · `404` · `500` |
| POST | `/create` | Crea una playlist | `PlaylistRequestDTO` | `201` · `400` · `404` · `409` |
| PUT | `/update/{id}` | Actualiza una playlist (Long) | `PlaylistRequestDTO` | `204` · `404` · `400` |
| DELETE | `/delete/{id}` | Elimina una playlist (Long) | — | `204` · `404` |

**Canciones dentro de una playlist:**

| Método | Ruta | Descripción | Body | Respuesta |
|--------|------|-------------|------|-----------|
| GET | `/{id}/songs` | Canciones de la playlist (Long), enriquecidas y **sin N+1**, en el orden en que se añadieron | — | `200` → `SongResponseDTO[]` · `404` |
| POST | `/{id}/songs` | Añade una canción a la playlist | `PlaylistSongRequestDTO` | `201` (sin body) · `400` · `404` |
| DELETE | `/{id}/songs/{songId}` | Quita una canción de la playlist (Long) | — | `204` (sin body) · `400` |

- `GET /{id}/songs` devuelve el **mismo `SongResponseDTO` enriquecido** (`artistName`, `albumTitle`,
  `albumCover`, `genres`) que `GET /songs/all` y `GET /favorites/songs/by-user/{userId}`, resuelto sin N+1.
- `POST /{id}/songs` es **idempotente**: añadir una canción ya presente responde `201` sin duplicarla.
  `404` si la playlist o la canción no existen.
- `DELETE /{id}/songs/{songId}` es **idempotente**: quitar algo que no estaba responde `204` (no `404`).

**`PlaylistRequestDTO` (request):**
```json
{
  "name": "Mi Lista de Rock",        // requerido, 3-255 caracteres
  "description": "Colección...",      // opcional, máx 1000 caracteres
  "isPublic": false,                  // requerido (default false)
  "coverImage": "https://example.com/image.jpg", // opcional, máx 255, debe empezar por http:// o https:// (o vacío)
  "userId": "123e4567-e89b-12d3-a456-426614174000" // requerido (UUID del propietario)
}
```

**`PlaylistSongRequestDTO` (request — body de `POST /{id}/songs`):**
```json
{
  "songId": 10                        // requerido (Long)
}
```

**`PlaylistResponseDTO` (response):**
```json
{
  "playlistId": 1,
  "name": "Mi Lista de Rock",
  "creationDate": "2024-01-01",
  "description": "Colección de mis canciones favoritas de rock",
  "isPublic": false,
  "coverImage": "https://example.com/image.jpg",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "songCount": 12                     // número de canciones en la playlist (0 si vacía)
}
```

---

## 6.9. Planes de suscripción
**Base:** `/api/v1/plans`

| Método | Ruta | Descripción | Body | Respuesta |
|--------|------|-------------|------|-----------|
| GET | `/all` | Lista todos los planes | — | `200` → `PlansSubscriptionResponseDTO[]` |
| GET | `/search/{id}` | Plan por ID (Long) | — | `200` → `PlansSubscriptionResponseDTO` · `404` |
| POST | `/create` | Crea un plan | `PlansSubscriptionRequestDTO` | `201` · `409` · `400` |
| PUT | `/update/{id}` | Actualiza un plan (Long) | `PlansUpdateRequestDTO` | `200` · `404` · `400` |
| DELETE | `/delete/{id}` | Elimina un plan (Long) | — | `204` · `404` |

**`PlansSubscriptionRequestDTO` (request — para crear):**
```json
{
  "interval": "MONTH",               // requerido (PlanInterval: DAY|WEEK|MONTH|YEAR)
  "features": ["NO_ADS", "PREMIUM_ACCOUNT"], // opcional (Set<SubscriptionFeature>)
  "setupFee": 0.00,                  // opcional
  "planName": "Plan Premium",        // requerido
  "price": 19.99,                    // requerido, positivo
  "duration": 12,                    // requerido, positivo (meses)
  "description": "Acceso ilimitado...", // requerido, no vacío
  "isActive": true,                  // opcional (default true)
  "freeTrial": true,                 // opcional
  "durationFreeTrial": 7             // opcional (días)
}
```

**`PlansUpdateRequestDTO` (request — para actualizar; solo estos campos):**
```json
{
  "setupFee": 0.00,                  // opcional
  "planName": "Plan Premium",        // requerido, no vacío
  "description": "Acceso ilimitado..." // requerido, no vacío
}
```

**`PlansSubscriptionResponseDTO` (response):**
```json
{
  "planId": 1,
  "paypalPlanId": "P-0NJ10521L3680291SOAQIVTQ",
  "interval": "MONTH",
  "features": ["AD_FREE", "UNLIMITED_SKIPS"],
  "setupFee": 0.00,
  "planName": "Plan Premium",
  "price": 19.99,
  "duration": 12,
  "description": "Acceso ilimitado a música sin anuncios.",
  "isActive": true,
  "freeTrial": true,
  "durationFreeTrial": 7
}
```

---

## 6.10. Suscripciones
**Base:** `/api/v1/subscriptions`

Integración con **PayPal**: el alta de una suscripción genera un token/URL de aprobación que el
frontend debe usar para redirigir al usuario a PayPal; tras la aprobación se ejecuta la suscripción.

| Método | Ruta | Descripción | Body | Respuesta |
|--------|------|-------------|------|-----------|
| POST | `/create` | Crea la suscripción y devuelve el **token de aprobación** de PayPal | `SubscriptionRequestDTO` | `200` → `String` (token/URL) |
| GET | `/execute/{token}` | Ejecuta/confirma la suscripción tras la aprobación en PayPal | — | `200` (sin body) |
| POST | `/create-user` | ⚠️ Endpoint de **prueba**: crea un usuario fijo de ejemplo | — | `200` |
| POST | `/create-payment-method` | ⚠️ Endpoint de **prueba**: crea un método de pago fijo de ejemplo | — | `200` |

**`SubscriptionRequestDTO` (request):**
```json
{
  "idUsuario": "123e4567-e89b-12d3-a456-426614174000", // requerido (UUID)
  "idPlan": 1,                                          // requerido (Long)
  "paypalSubscriptionId": "I-XXXXXX",                   // opcional, máx 255
  "idMetodoPago": 1,                                    // requerido (Long)
  "fechaInicio": "2026-01-01T00:00:00",                 // requerido (LocalDateTime)
  "fechaRenovacion": "2026-02-01T00:00:00",             // requerido (LocalDateTime)
  "fechaCancelacion": null,                             // opcional
  "estado": "PENDIG"                                    // opcional (SubscriptionState)
}
```

**`SubscriptionResponseDTO`** (definido en el código; útil como referencia de la entidad suscripción):
```json
{
  "idSuscripcion": 1,
  "idUsuario": "123e4567-e89b-12d3-a456-426614174000",
  "idPlan": 1,
  "paypalSubscriptionId": "I-XXXXXX",
  "idMetodoPago": 1,
  "fechaInicio": "2026-01-01T00:00:00",
  "fechaRenovacion": "2026-02-01T00:00:00",
  "fechaCancelacion": null,
  "estado": "ACTIVE"
}
```

**Flujo recomendado en frontend:**
1. `POST /create` → recibir el `token` (string).
2. Redirigir al usuario a la URL de aprobación de PayPal (con ese token).
3. Tras la aprobación, PayPal redirige al `success-url` configurado; el frontend (o el callback) llama a `GET /execute/{token}`.

> 🧪 `create-user` y `create-payment-method` son endpoints de prueba con datos hardcodeados; **no usar en producción**.

---

## 6.11. Favoritos
**Base:** `/api/v1/favorites`

Favoritos por usuario para **canciones** ("me gusta") y **álbumes** ("favoritos"). Todas las
mutaciones son **idempotentes**: marcar dos veces no produce error y quitar algo que no estaba
tampoco (no devuelve `404` ni `409`).

**Canciones (likes):**

| Método | Ruta | Descripción | Body | Respuesta |
|--------|------|-------------|------|-----------|
| POST | `/songs` | El usuario da like a una canción | `FavoriteSongRequestDTO` | `201` (sin body) · `400` · `404` |
| DELETE | `/songs` | El usuario quita el like | `FavoriteSongRequestDTO` | `200` (sin body) · `400` |
| GET | `/songs/by-user/{userId}` | Canciones favoritas del usuario (UUID) | — | `200` → `SongResponseDTO[]` · `404` |

**Álbumes (favoritos):**

| Método | Ruta | Descripción | Body | Respuesta |
|--------|------|-------------|------|-----------|
| POST | `/albums` | El usuario marca un álbum como favorito | `FavoriteAlbumRequestDTO` | `201` (sin body) · `400` · `404` |
| DELETE | `/albums` | El usuario lo quita de favoritos | `FavoriteAlbumRequestDTO` | `200` (sin body) · `400` |
| GET | `/albums/by-user/{userId}` | Álbumes favoritos del usuario (UUID) | — | `200` → `AlbumResponseDTO[]` · `404` |

**`FavoriteSongRequestDTO` (request):**
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",  // requerido (UUID)
  "songId": 10                                         // requerido (Long)
}
```

**`FavoriteAlbumRequestDTO` (request):**
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",  // requerido (UUID)
  "albumId": 2                                         // requerido (Long)
}
```

- `GET /songs/by-user/{userId}` devuelve el **mismo `SongResponseDTO` enriquecido** (artista, álbum,
  portada y géneros) que `GET /songs/all`, resuelto **sin N+1**; las más recientes primero.
- `GET /albums/by-user/{userId}` devuelve `AlbumResponseDTO[]` (con su `artista` anidado), las más recientes primero.

**Comportamiento y casos borde:**
- **POST** (like/favorito): si `userId`, `songId` o `albumId` **no existe** → `404` (texto plano). Si ya estaba
  marcado, es idempotente: responde `201` sin error (no `409`).
- **DELETE** (unlike/quitar): idempotente. Quitar algo que no estaba responde `200` (no `404`). Solo valida que
  los IDs no sean nulos (`400`).
- **GET** por usuario: usuario **sin favoritos** → `200 []`. Usuario **inexistente** → `404`.

> ⚠️ Igual que en Seguidores, `POST`/`DELETE` **no devuelven body**; usar solo el status code.
> `DELETE /songs` y `DELETE /albums` llevan **body JSON** en la petición (poco habitual para un DELETE, pero así está implementado).

---

## 6.12. Archivos / imágenes
**Base:** `/uploads` *(oculto en Swagger — `@Hidden`)*

| Método | Ruta | Descripción | Respuesta |
|--------|------|-------------|-----------|
| GET | `/uploads/{type}/{filename}` | Sirve un archivo subido | `200` (binario) · `400` (tipo inválido) · `404` |

- **`type`** válidos: `images` (→ `uploads/images`) y `profiles` (→ `uploads/profiles`).
- El `Content-Type` se detecta automáticamente; se sirve `inline`.
- Los nombres de archivo se sanitizan y hay protección contra *path traversal*.
- Ejemplo: `http://localhost:8080/uploads/images/default_cover_art.png`

> Muchas rutas de imagen devueltas por otros DTOs (`imgProfile`, `coverImg`, `albumCoverPath`, `portada`)
> apuntan a este endpoint o a rutas servidas estáticamente; para mostrarlas, anteponer la Base URL si la ruta es relativa.

---

## 7. Notas rápidas para el agente de implementación frontend

1. **No fijar `Content-Type` manualmente** en peticiones `multipart/form-data` (dejar que el navegador
   ponga el `boundary`). Aplica a: álbumes (`create`/`update`), canciones (`analyze`/`create`/`update`)
   y artistas (`create`/`update`).
2. **Partes JSON en multipart** (`albumRequestDTO`, `songRequest`, `artistRequestDTO`): enviarlas como
   `Blob`/parte con `type: "application/json"`, no como string plano.
3. **IDs**: usuarios y perfiles usan **UUID** (string); artistas, álbumes, canciones, géneros, playlists y planes usan **Long** (number).
4. **Respuestas sin body**: varias operaciones (`204`, follow/unfollow, execute) no devuelven contenido; basarse en el status.
5. **Respuestas en texto plano**: los endpoints de álbumes (`create`/`update`/`delete`), el token de suscripción
   y la imagen de perfil devuelven **string**, no JSON.
6. **Errores**: validación → objeto `{campo: mensaje}` (400); negocio → string. Manejar ambos casos.
7. **Streaming de audio**: usar directamente la URL `/api/v1/songs/stream/{id}` en el reproductor; soporta `Range`.
8. **Autenticación**: hoy abierta (`permitAll`), pero preparar el envío de `Authorization: Bearer <JWT>` para cuando se active.
```
