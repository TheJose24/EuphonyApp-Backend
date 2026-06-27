package com.euphony.streaming.service.implementation;

import com.euphony.streaming.dto.request.SongRequestDTO;
import com.euphony.streaming.dto.response.SongMetadataResponseDTO;
import com.euphony.streaming.dto.response.SongResponseDTO;
import com.euphony.streaming.entity.*;
import com.euphony.streaming.exception.custom.album.AlbumNotFoundException;
import com.euphony.streaming.exception.custom.artist.ArtistNotFoundException;
import com.euphony.streaming.exception.custom.song.SongNotFoundException;
import com.euphony.streaming.repository.*;
import com.euphony.streaming.service.interfaces.IFileStorageService;
import com.euphony.streaming.service.interfaces.ISongMetadataService;
import com.euphony.streaming.service.interfaces.ISongService;
import com.euphony.streaming.util.SongGenreProjection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__(@Lazy))
@CacheConfig(cacheNames = {"songs", "songGenres"})
public class SongServiceImpl implements ISongService {

    private final CancionRepository cancionRepository;
    private final GeneroRepository generoRepository;
    private final CancionGeneroRepository cancionGeneroRepository;
    private final ArtistaRepository artistaRepository;
    private final AlbumRepository albumRepository;
    private final ISongMetadataService songMetadataService;
    private final IFileStorageService fileStorageService;

    private static final String ERROR_ARTIST_NOT_FOUND = "Artista no encontrado: %s";
    private static final String ERROR_ALBUM_NOT_FOUND = "Álbum no encontrado";
    private static final String ERROR_SONG_NOT_FOUND = "Canción no encontrada con ID %d";
    private static final String ERROR_ALBUM_NOT_FOUND_BY_ID = "Álbum no encontrado con ID %d";
    private static final String ERROR_ARTIST_NOT_FOUND_BY_ID = "Artista no encontrado con ID %d";

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "songs", key = "'all'")
    public List<SongResponseDTO> findAllSongs() {
        log.info("Buscando todas las canciones");
        return toSongResponseDTOs(cancionRepository.findAllWithArtistAndAlbum());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "songs", key = "#songId")
    public SongResponseDTO searchSongById(Long songId) {
        log.info("Buscando canción por id: {}", songId);
        return cancionRepository.findByIdWithArtistAndAlbum(songId)
                .map(song -> mapToSongResponseDTO(song, fetchGenresForSong(song.getIdCancion())))
                .orElseThrow(() -> new SongNotFoundException(
                        String.format(ERROR_SONG_NOT_FOUND, songId),
                        HttpStatus.NOT_FOUND
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SongResponseDTO> findSongsByAlbum(Long albumId) {
        log.info("Buscando canciones del álbum: {}", albumId);
        if (!albumRepository.existsById(albumId)) {
            throw new AlbumNotFoundException(
                    String.format(ERROR_ALBUM_NOT_FOUND_BY_ID, albumId), HttpStatus.NOT_FOUND);
        }
        return toSongResponseDTOs(cancionRepository.findByAlbumIdWithArtistAndAlbum(albumId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SongResponseDTO> findSongsByArtist(Long artistId) {
        log.info("Buscando canciones del artista: {}", artistId);
        if (!artistaRepository.existsById(artistId)) {
            throw new ArtistNotFoundException(
                    String.format(ERROR_ARTIST_NOT_FOUND_BY_ID, artistId), HttpStatus.NOT_FOUND);
        }
        return toSongResponseDTOs(cancionRepository.findByArtistIdWithArtistAndAlbum(artistId));
    }

    @Override
    public Path getSongFilePath(Long id) {
        CancionEntity song = cancionRepository.findById(id)
                .orElseThrow(() -> new SongNotFoundException("Canción no encontrada", HttpStatus.NOT_FOUND));
        log.info("Obteniendo la ruta del archivo de la canción: {}", song.getTitulo());
        String baseUploadDir = "uploads";
        // Normalizar la ruta eliminando "/uploads/" si existe
        String normalizedPath = song.getRutaArchivo().replace('\\', '/');
        if (normalizedPath.startsWith("/uploads/")) {
            normalizedPath = normalizedPath.substring("/uploads/".length());
        }
        Path path = Paths.get(baseUploadDir, normalizedPath).normalize();
        log.info("Ruta del archivo de la canción: {}", path);
        return path;
    }

    @Override
    @Transactional
    public SongMetadataResponseDTO analyzeSong(MultipartFile songFile) throws IOException {
        log.info("Analizando metadatos de la canción sin guardarla");
        SongMetadataResponseDTO metadata = songMetadataService.analyzeSongMetadata(songFile);

        return SongMetadataResponseDTO.builder()
                .title(metadata.getTitle())
                .artist(metadata.getArtist())
                .album(metadata.getAlbum())
                .releaseDate(metadata.getReleaseDate())
                .lyrics(metadata.getLyrics())
                .genres(metadata.getGenres())
                .duration(metadata.getDuration())
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = {"songs", "songGenres"}, allEntries = true)
    public void createSong(MultipartFile song, SongRequestDTO songRequestDTO) {
        log.info("Creando nueva canción: {}", songRequestDTO.getTitle());

        // Buscar el álbum
        AlbumEntity album = albumRepository.findByTitulo(songRequestDTO.getAlbum())
                .orElseThrow(() -> new AlbumNotFoundException(ERROR_ALBUM_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Obtener el artista
        ArtistaEntity artist = artistaRepository.findByNombre(songRequestDTO.getArtist())
                .orElseThrow(() -> new ArtistNotFoundException(
                        String.format(ERROR_ARTIST_NOT_FOUND, songRequestDTO.getArtist()),
                        HttpStatus.NOT_FOUND
                ));

        // Guardar archivo de canción
        String songPath = fileStorageService.storeFile(song, "AUDIO");

        log.info("Géneros recibidos: {}", songRequestDTO.getGenres());

        // Leer los metadatos de la canción
        SongMetadataResponseDTO songMetadata = new SongMetadataResponseDTO();
        songMetadata.setFilePath(songPath);
        songMetadata.setTitle(songRequestDTO.getTitle());
        songMetadata.setArtist(songRequestDTO.getArtist());
        songMetadata.setAlbum(songRequestDTO.getAlbum());
        songMetadata.setGenres(songRequestDTO.getGenres());
        songMetadata.setDuration(songRequestDTO.getDuration());
        songMetadata.setLyrics(songRequestDTO.getLyrics());
        songMetadata.setReleaseDate(songRequestDTO.getReleaseDate());
        songMetadata.setAlbumCoverPath(album.getPortada());

        log.info("Metadatos de la canción: {}", songMetadata);

        // Actualizar los metadatos del archivo MP3
        songMetadataService.assignMetadata(songMetadata);

        log.info("Metadatos de la canción actualizados: {}", songMetadata);

        // Mapear a la entidad CancionEntity
        CancionEntity songEntity = buildSongEntity(songMetadata, artist, album, songPath, songRequestDTO.getLanguage());

        // Guardar la canción
        CancionEntity savedSong = cancionRepository.save(songEntity);

        // Asociar géneros
        saveGenresForSong(savedSong, songRequestDTO.getGenres());

        log.info("Canción '{}' creada exitosamente", savedSong.getTitulo());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"songs", "songGenres"}, key = "#songId")
    public void deleteSong(Long songId) {
        log.info("Eliminando canción con ID: {}", songId);
        CancionEntity song = cancionRepository.findById(songId)
                .orElseThrow(() -> new SongNotFoundException(
                        String.format(ERROR_SONG_NOT_FOUND, songId),
                        HttpStatus.NOT_FOUND
                ));

        // Eliminar archivo físico de la canción y la portada
        fileStorageService.deleteFile(song.getRutaArchivo());

        // Eliminar la portada del álbum si no es compartida con otras canciones
        if (song.getAlbum() != null && !song.getAlbum().getPortada().equals(song.getPortada())) {
            fileStorageService.deleteFile(song.getPortada());
        }

        // Eliminar la canción de la base de datos
        cancionRepository.delete(song);
        log.info("Canción con ID '{}' eliminada exitosamente", songId);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"songs", "songGenres"}, key = "#songId")
    public void updateSong(Long songId, MultipartFile coverArtFile, SongRequestDTO songRequestDTO) throws IOException {
        log.info("Actualizando canción con ID: {}", songId);

        // Buscar la canción existente
        CancionEntity existingSong = cancionRepository.findById(songId)
                .orElseThrow(() -> new SongNotFoundException(
                        String.format(ERROR_SONG_NOT_FOUND, songId),
                        HttpStatus.NOT_FOUND
                ));

        ArtistaEntity artist = artistaRepository.findByNombre(songRequestDTO.getArtist())
                .orElseThrow(() -> new ArtistNotFoundException(
                        String.format(ERROR_ARTIST_NOT_FOUND, songRequestDTO.getArtist()),
                        HttpStatus.NOT_FOUND
                ));

        // Manejar la portada (si se proporciona)
        String newCoverArtPath = existingSong.getPortada();
        if (coverArtFile != null && !coverArtFile.isEmpty()) {
            // Eliminar la portada antigua si no es compartida con otras canciones
            if (existingSong.getAlbum() != null && !existingSong.getAlbum().getPortada().equals(existingSong.getPortada())) {
                fileStorageService.deleteFile(existingSong.getPortada());
            }
            newCoverArtPath = fileStorageService.storeFile(coverArtFile, "IMAGE");
        }

        SongMetadataResponseDTO songMetadata = new SongMetadataResponseDTO();
        songMetadata.setFilePath(existingSong.getRutaArchivo());
        songMetadata.setTitle(songRequestDTO.getTitle());
        songMetadata.setArtist(songRequestDTO.getArtist());
        songMetadata.setAlbum(existingSong.getAlbum().getTitulo());
        songMetadata.setGenres(songRequestDTO.getGenres());
        songMetadata.setDuration(songRequestDTO.getDuration());
        songMetadata.setLyrics(songRequestDTO.getLyrics());
        songMetadata.setReleaseDate(songRequestDTO.getReleaseDate());
        songMetadata.setAlbumCoverPath(newCoverArtPath);



        // Mapear los cambios en la entidad CancionEntity
        updateSongEntity(existingSong, songRequestDTO, songMetadata, artist, newCoverArtPath);

        // Actualizar los metadatos del archivo MP3
        songMetadataService.assignMetadata(songMetadata);

        // Guardar la canción actualizada
        CancionEntity updatedSong = cancionRepository.save(existingSong);

        // Asociar géneros (si corresponde)
        updateSongGenres(updatedSong, songRequestDTO.getGenres());

        log.info("Canción con ID '{}' actualizada exitosamente", updatedSong.getIdCancion());
    }


    /**
     * Construye una entidad CancionEntity para una nueva canción.
     */
    private CancionEntity buildSongEntity(SongMetadataResponseDTO songMetadata,
                                          ArtistaEntity artist,
                                          AlbumEntity album,
                                          String songPath,
                                          String language) {
        CancionEntity songEntity = new CancionEntity();
        songEntity.setTitulo(songMetadata.getTitle());
        songEntity.setPortada(songMetadata.getAlbumCoverPath());
        songEntity.setRutaArchivo(songPath);
        songEntity.setDuracion(songMetadata.getDuration());
        songEntity.setArtista(artist);
        songEntity.setAlbum(album);
        songEntity.setAnioLanzamiento(songMetadata.getReleaseDate());
        songEntity.setLetra(songMetadata.getLyrics());
        songEntity.setIdioma(language);
        songEntity.setCalificacionPromedio(BigDecimal.ZERO);
        return songEntity;
    }

    /**
     * Actualiza una entidad CancionEntity existente.
     */
    private void updateSongEntity(
            CancionEntity existingSong,
            SongRequestDTO songRequestDTO,
            SongMetadataResponseDTO songMetadataResponseDTO,
            ArtistaEntity artist,
            String coverArtPath
    ) {
        Optional.ofNullable(songRequestDTO.getTitle())
                .filter(StringUtils::hasText)
                .ifPresent(existingSong::setTitulo);

        Optional.ofNullable(artist)
                .ifPresent(existingSong::setArtista);

        Optional.ofNullable(coverArtPath)
                .filter(StringUtils::hasText)
                .ifPresent(existingSong::setPortada);

        Optional.ofNullable(songMetadataResponseDTO.getDuration())
                .ifPresent(existingSong::setDuracion);

        Optional.ofNullable(songRequestDTO.getLanguage())
                .filter(StringUtils::hasText)
                .ifPresent(existingSong::setIdioma);

        Optional.ofNullable(songMetadataResponseDTO.getLyrics())
                .filter(StringUtils::hasText)
                .ifPresent(existingSong::setLetra);

        Optional.ofNullable(songMetadataResponseDTO.getReleaseDate())
                .ifPresent(existingSong::setAnioLanzamiento);
    }

    /**
     * Guarda los géneros para una canción.
     */
    private void saveGenresForSong(CancionEntity song, Set<String> genres) {
        if (genres != null) {
            genres.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(genre -> !genre.isEmpty())
                    .distinct()
                    .forEach(genreName -> saveSingleSongGenre(song, genreName));
        }
    }

    /**
     * Actualiza los géneros de una canción.
     */
    private void updateSongGenres(CancionEntity song, Set<String> genres) {
        // Eliminar géneros existentes
        cancionGeneroRepository.deleteByCancion(song);

        // Guardar nuevos géneros
        saveGenresForSong(song, genres);
    }

    /**
     * Guarda un género individual para una canción.
     */
    private void saveSingleSongGenre(CancionEntity song, String genreName) {
        if (genreName == null || genreName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del género no puede estar vacío.");
        }

        GeneroEntity genre = generoRepository.findByNombre(genreName.trim())
                .orElseGet(() -> {
                    log.info("Creando nuevo género: {}", genreName);
                    GeneroEntity newGenre = new GeneroEntity();
                    newGenre.setNombre(genreName.trim());
                    return generoRepository.save(newGenre);
                });

        CancionGeneroEntity songGenre = new CancionGeneroEntity();
        songGenre.setCancion(song);
        songGenre.setGenero(genre);
        cancionGeneroRepository.save(songGenre);
        log.info("Género '{}' asociado a la canción '{}'", genreName, song.getTitulo());
    }

    /**
     * Convierte una lista de canciones en sus DTOs, resolviendo los géneros de todas
     * ellas en una sola consulta batcheada (evita N+1). Patrón compartido por los
     * listados: todas, por álbum y por artista.
     */
    private List<SongResponseDTO> toSongResponseDTOs(List<CancionEntity> songs) {
        Map<Long, Set<String>> genresBySong = fetchGenresBySongIds(
                songs.stream().map(CancionEntity::getIdCancion).toList());
        return songs.stream()
                .map(song -> mapToSongResponseDTO(song,
                        genresBySong.getOrDefault(song.getIdCancion(), Set.of())))
                .toList();
    }

    /**
     * Mapea una entidad CancionEntity a un SongResponseDTO usando los géneros ya resueltos.
     */
    private SongResponseDTO mapToSongResponseDTO(CancionEntity cancionEntity, Set<String> genres) {
        ArtistaEntity artista = cancionEntity.getArtista();
        AlbumEntity album = cancionEntity.getAlbum();

        return SongResponseDTO.builder()
                .songId(cancionEntity.getIdCancion())
                .artistId(Optional.ofNullable(artista)
                        .map(ArtistaEntity::getIdArtista)
                        .orElse(null))
                .artistName(Optional.ofNullable(artista)
                        .map(ArtistaEntity::getNombre)
                        .orElse(null))
                .albumId(Optional.ofNullable(album)
                        .map(AlbumEntity::getIdAlbum)
                        .orElse(null))
                .albumTitle(Optional.ofNullable(album)
                        .map(AlbumEntity::getTitulo)
                        .orElse(null))
                .albumCover(Optional.ofNullable(album)
                        .map(AlbumEntity::getPortada)
                        .orElse(null))
                .title(cancionEntity.getTitulo())
                .coverImg(cancionEntity.getPortada())
                .duration(cancionEntity.getDuracion())
                .language(cancionEntity.getIdioma())
                .lyrics(cancionEntity.getLetra())
                .releaseDate(cancionEntity.getAnioLanzamiento())
                .filePath(cancionEntity.getRutaArchivo())
                .averageRating(cancionEntity.getCalificacionPromedio())
                .numberOfPlays(cancionEntity.getNumeroReproducciones())
                .genres(genres)
                .build();
    }

    /**
     * Recupera los géneros para una canción.
     * Se usa un TreeSet para devolverlos en orden alfabético determinista.
     */
    private Set<String> fetchGenresForSong(Long songId) {
        return cancionGeneroRepository.findByCancion_IdCancion(songId).stream()
                .map(CancionGeneroEntity::getGenero)
                .filter(Objects::nonNull)
                .map(GeneroEntity::getNombre)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Recupera los géneros de varias canciones en una sola consulta y los agrupa por
     * ID de canción. Evita el problema N+1 al listar canciones con sus géneros.
     * Si no hay canciones, evita ejecutar la consulta (no genera un {@code IN ()}).
     * Cada conjunto es un TreeSet para devolver los géneros en orden alfabético determinista.
     */
    private Map<Long, Set<String>> fetchGenresBySongIds(List<Long> songIds) {
        if (songIds.isEmpty()) {
            return Map.of();
        }
        return cancionGeneroRepository.findGenresByCancionIds(songIds).stream()
                .filter(projection -> projection.getGenreName() != null)
                .collect(Collectors.groupingBy(
                        SongGenreProjection::getSongId,
                        Collectors.mapping(SongGenreProjection::getGenreName,
                                Collectors.toCollection(TreeSet::new))));
    }
}