package com.euphony.streaming.service.interfaces;

import com.euphony.streaming.dto.request.ArtistRequestDTO;
import com.euphony.streaming.dto.response.ArtistResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Interfaz que define las operaciones de gestión de artistas.
 */
public interface IArtistService {

    /**
     * Obtiene todos los artistas registrados en el sistema.
     *
     * @return Lista de {@link ArtistResponseDTO} con la información de todos los artistas.
     */
    List<ArtistResponseDTO> findAllArtists();

    /**
     * Obtiene información de un artista específico por su Nombre.
     *
     * @param name El nombre del artista.
     * @return Un {@link ArtistResponseDTO} con los datos del artista.
     */
    ArtistResponseDTO findArtistByName(String name);

    /**
     * Crea un nuevo artista en el sistema.
     *
     * @param artistRequestDTO Un objeto {@link ArtistRequestDTO} con los datos del nuevo artista.
     * @param imageFile Imagen del artista (opcional). Si es {@code null} o vacía, el artista queda sin imagen.
     */
    void createArtist(ArtistRequestDTO artistRequestDTO, MultipartFile imageFile);

    /**
     * Actualiza los datos de un artista existente.
     *
     * @param id El identificador único del artista a actualizar.
     * @param artistRequestDTO Un objeto {@link ArtistRequestDTO} con los datos actualizados.
     * @param imageFile Nueva imagen del artista (opcional). Si es {@code null} o vacía, se conserva la imagen actual.
     */
    void updateArtist(Long id, ArtistRequestDTO artistRequestDTO, MultipartFile imageFile);

    /**
     * Elimina un artista del sistema.
     *
     * @param id El identificador único del artista a eliminar.
     */
    void deleteArtist(Long id);
}
