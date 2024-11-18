package com.euphony.streaming.service.implementation;

import com.euphony.streaming.dto.request.AlbumRequestDTO;
import com.euphony.streaming.dto.response.AlbumResponseDTO;
import com.euphony.streaming.dto.response.ArtistResponseDTO;
import com.euphony.streaming.entity.AlbumEntity;
import com.euphony.streaming.entity.ArtistaEntity;
import com.euphony.streaming.exception.custom.album.*;
import com.euphony.streaming.exception.custom.storage.FileStorageException;
import com.euphony.streaming.repository.AlbumRepository;
import com.euphony.streaming.repository.ArtistaRepository;
import com.euphony.streaming.service.interfaces.IAlbumService;
import com.euphony.streaming.service.interfaces.IFileStorageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de gestión de álbumes.
 * Maneja las operaciones CRUD para álbumes incluyendo la gestión de imágenes de portada.
 * Incluye funcionalidades de caché para optimizar el rendimiento de las consultas frecuentes.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@CacheConfig(cacheNames = "albums")
public class AlbumServiceImpl implements IAlbumService {

    private static final String DEFAULT_COVER_PATH  = "/uploads/default_cover_art.png";
    private static final String CONTENT_TYPE_IMAGE  = "IMAGE";
    private static final String ALBUM_NOT_FOUND_MESSAGE  = "Álbum no encontrado con %s: %s";
    private static final String ERROR_PROCESSING_MESSAGE  = "Error al procesar %s del álbum: %s";

    private final AlbumRepository albumRepository;
    private final ArtistaRepository artistaRepository;
    private final IFileStorageService fileStorageService;

    @Value("${app.album.title.max-length}")
    private int maxTitleLength;

    @Cacheable
    @Override
    public List<AlbumResponseDTO> findAllAlbums() {
        log.debug("Buscando todos los álbumes");
        return albumRepository.findAll().stream()
                .map(this::mapToAlbumResponseDTO)
                .toList();
    }

    @Cacheable(key = "#name")
    @Override
    public AlbumResponseDTO findAlbumByName(String name) {
        log.debug("Buscando álbum con el nombre: {}", name);
        return albumRepository.findByTitulo(name)
                .map(this::mapToAlbumResponseDTO)
                .orElseThrow(() -> new AlbumNotFoundException(
                        String.format(ALBUM_NOT_FOUND_MESSAGE, "nombre", name),
                        HttpStatus.NOT_FOUND
                ));
    }

    @Transactional
    @CacheEvict(allEntries = true)
    @Override
    public void createAlbum(@Valid @NotNull AlbumRequestDTO albumRequestDTO,
                            MultipartFile coverImage) {
        log.debug("Creando nuevo álbum con el título: {}", albumRequestDTO.getTitle());

        try {
            validateAlbumRequest(albumRequestDTO);
            validateTitleLength(albumRequestDTO.getTitle());
            validateTitleUniqueness(albumRequestDTO.getTitle());

            String coverImagePath = processCoverImage(coverImage);
            AlbumEntity albumEntity = createAlbumEntity(albumRequestDTO, coverImagePath);

            albumRepository.save(albumEntity);
            log.info("Álbum creado exitosamente: {}", albumEntity.getTitulo());
        } catch (FileStorageException e) {
            handleFileStorageException(e, "crear", albumRequestDTO.getTitle());
        }
    }

    @Transactional
    @CacheEvict(allEntries = true)
    @Override
    public void updateAlbum(Long id, @Valid @NotNull AlbumRequestDTO albumRequestDTO,
                            MultipartFile coverImage) {
        log.debug("Actualizando álbum con ID: {}", id);

        AlbumEntity albumEntity = albumRepository.findById(id)
                .orElseThrow(() -> new AlbumNotFoundException(
                        String.format(ALBUM_NOT_FOUND_MESSAGE, "nombre", albumRequestDTO.getTitle()),
                        HttpStatus.NOT_FOUND
                ));

        try {
            validateAlbumUpdateRequest(albumRequestDTO, id);
            ArtistaEntity artista = findArtistById(albumRequestDTO.getIdArtist());
            String coverImagePath = updateCoverImage(coverImage, albumEntity.getPortada());

            updateAlbumFields(albumEntity, albumRequestDTO, artista, coverImagePath);
            albumRepository.save(albumEntity);

            log.info("Álbum actualizado exitosamente: {}", albumEntity.getTitulo());
        } catch (FileStorageException e) {
            handleFileStorageException(e, "actualizar", albumRequestDTO.getTitle());
        } catch (Exception e) {
            handleGeneralException(e, "actualizar", albumRequestDTO.getTitle());
        }
    }

    @Transactional
    @CacheEvict(allEntries = true)
    @Override
    public void deleteAlbum(Long id) {
        log.debug("Eliminando álbum con ID: {}", id);

            if (!albumRepository.existsById(id)) {
                throw new AlbumNotFoundException(
                        String.format(ALBUM_NOT_FOUND_MESSAGE, "id", id),
                        HttpStatus.NOT_FOUND
                );
            }

            // Obtener la portada antes de eliminar para poder eliminarla después
            Optional<String> coverPath = albumRepository.findById(id)
                    .map(AlbumEntity::getPortada)
                    .filter(path -> !path.equals(DEFAULT_COVER_PATH));



            // Eliminar la imagen de portada si existe y no es la predeterminada
            coverPath.ifPresent(path -> {
                if (!DEFAULT_COVER_PATH.equals(path)) {
                    try {
                        fileStorageService.deleteFile(path);
                    } catch (FileStorageException e) {
                        log.warn("No se pudo eliminar la imagen de portada: {}", path, e);
                        throw new AlbumDeletionException("Ocurrió un error al eliminar la imagen de portada", HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }
            });

            albumRepository.deleteById(id);

            log.info("Álbum eliminado exitosamente con ID: {}", id);
    }

    private void validateAlbumUpdateRequest(AlbumRequestDTO albumRequestDTO, Long id) {
        validateAlbumRequest(albumRequestDTO);
        validateTitleLength(albumRequestDTO.getTitle());

        // Verificar si el título ya existe para otro álbum
        albumRepository.findByTitulo(albumRequestDTO.getTitle())
                .ifPresent(existingAlbum -> {
                    if (!existingAlbum.getIdAlbum().equals(id)) {
                        throw new AlbumUpdateException(
                                "Ya existe un álbum con el título: " + albumRequestDTO.getTitle(),
                                HttpStatus.CONFLICT
                        );
                    }
                });
    }

    private void validateTitleLength(String title) {
        if (title.length() > maxTitleLength) {
            throw new AlbumValidationException(
                    String.format("El título del álbum no puede exceder los %d caracteres", maxTitleLength),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void validateTitleUniqueness(String title) {
        if (albumRepository.findByTitulo(title).isPresent()) {
            throw new AlbumCreationException(
                    "Ya existe un álbum con el título: " + title,
                    HttpStatus.CONFLICT
            );
        }
    }

    private String processCoverImage(MultipartFile coverImage) {
        if (coverImage == null || coverImage.isEmpty()) {
            log.info("No se recibió imagen, asignando imagen predeterminada");
            return DEFAULT_COVER_PATH;
        }

        return fileStorageService.storeFile(coverImage, CONTENT_TYPE_IMAGE);
    }

    private String updateCoverImage(MultipartFile newImage, String currentImagePath) {
        if (newImage == null || newImage.isEmpty()) {
            return currentImagePath;
        }

        String newImagePath = fileStorageService.storeFile(newImage, CONTENT_TYPE_IMAGE);

        // Eliminar la imagen anterior si no es la predeterminada
        if (!DEFAULT_COVER_PATH.equals(currentImagePath)) {
            try {
                fileStorageService.deleteFile(currentImagePath);
            } catch (FileStorageException e) {
                log.warn("No se pudo eliminar la imagen anterior: {}", currentImagePath, e);
            }
        }

        return newImagePath;
    }

    private ArtistaEntity findArtistById(Long artistId) {
        return artistaRepository.findById(artistId)
                .orElseThrow(() -> new AlbumUpdateException(
                        "Artista no encontrado con el ID: " + artistId,
                        HttpStatus.BAD_REQUEST
                ));
    }

    private void updateAlbumFields(AlbumEntity album, AlbumRequestDTO request,
                                   ArtistaEntity artista, String coverImagePath) {
        Optional.ofNullable(request.getTitle()).ifPresent(titulo -> album.setTitulo(titulo.trim()));
        Optional.ofNullable(request.getReleaseDate()).ifPresent(album::setFechaLanzamiento);
        Optional.ofNullable(coverImagePath).ifPresent(album::setPortada);
        Optional.ofNullable(artista).ifPresent(album::setArtista);
    }

    private void validateAlbumRequest(AlbumRequestDTO albumRequestDTO) {
        if (albumRequestDTO.getIdArtist() == null) {
            throw new AlbumValidationException("El ID del artista no puede estar vacío", HttpStatus.BAD_REQUEST);
        }
        if (albumRequestDTO.getTitle() == null || albumRequestDTO.getTitle().trim().isEmpty()) {
            throw new AlbumValidationException("El título del álbum no puede estar vacío", HttpStatus.BAD_REQUEST);
        }
    }

    private AlbumEntity createAlbumEntity(AlbumRequestDTO albumRequestDTO, String coverImagePath) {
        AlbumEntity albumEntity = new AlbumEntity();
        albumEntity.setTitulo(albumRequestDTO.getTitle());
        albumEntity.setFechaLanzamiento(albumRequestDTO.getReleaseDate());
        albumEntity.setPortada(coverImagePath);
        albumEntity.setArtista(albumRequestDTO.getIdArtist() != null
                ? artistaRepository.findById(albumRequestDTO.getIdArtist())
                .orElseThrow(() -> new AlbumCreationException("Artista no encontrado con el ID: " + albumRequestDTO.getIdArtist(), HttpStatus.NOT_FOUND))
                : null);
        return albumEntity;
    }

    private void handleFileStorageException(FileStorageException e, String action, String identifier) {
        String errorMessage = String.format(ERROR_PROCESSING_MESSAGE, action, identifier);
        log.error(errorMessage, e);
        throw new AlbumStorageException(errorMessage, e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void handleGeneralException(Exception e, String action, String identifier) {
        String errorMessage = String.format("Error al %s el álbum: %s", action, identifier);
        log.error(errorMessage, e);
        throw new AlbumOperationException(errorMessage, e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private AlbumResponseDTO mapToAlbumResponseDTO(AlbumEntity albumEntity) {
        return AlbumResponseDTO.builder()
                .idAlbum(albumEntity.getIdAlbum())
                .artista(mapToArtistResponseDTO(albumEntity.getArtista()))
                .titulo(albumEntity.getTitulo())
                .fechaLanzamiento(albumEntity.getFechaLanzamiento().toString())
                .portada(albumEntity.getPortada())
                .build();
    }

    private ArtistResponseDTO mapToArtistResponseDTO(ArtistaEntity artistaEntity) {
        return ArtistResponseDTO.builder()
                .idArtist(artistaEntity.getIdArtista())
                .name(artistaEntity.getNombre())
                .biography(artistaEntity.getBiografia())
                .country(artistaEntity.getPais())
                .socialNetworks(artistaEntity.getRedesSociales())
                .isVerified(artistaEntity.getIsVerified())
                .build();
    }
}