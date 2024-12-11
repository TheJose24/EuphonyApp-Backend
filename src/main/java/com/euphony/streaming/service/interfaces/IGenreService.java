package com.euphony.streaming.service.interfaces;

import com.euphony.streaming.dto.request.GenreRequestDTO;
import com.euphony.streaming.dto.response.GenreMostPlayedDTO;
import com.euphony.streaming.dto.response.GenreResponseDTO;

import java.util.List;

/**
 * Interfaz que define las operaciones de gestión de géneros musicales.
 */
public interface IGenreService {

    /**
     * Obtiene todos los géneros musicales registrados en el sistema.
     *
     * @return Lista de {@link GenreResponseDTO} con la información de todos los géneros musicales.
     */
    List<GenreResponseDTO> findAllGenres();

    /**
     * Obtiene información de un género musical específico por su Nombre.
     *
     * @param name El nombre del género musical.
     * @return Un {@link GenreResponseDTO} con los datos del género musical.
     */
    GenreResponseDTO findGenreByName(String name);

    /**
     * Obtiene los géneros musicales más escuchados en el sistema.
     *
     * @param limit El número máximo de géneros a recuperar.
     * @return Lista de {@link GenreMostPlayedDTO} con los géneros más escuchados.
     */
    List<GenreMostPlayedDTO> findMostPlayedGenres(int limit);

    /**
     * Crea un nuevo género musical en el sistema.
     *
     * @param genreRequestDTO Un objeto {@link GenreRequestDTO} con los datos del nuevo género musical.
     */
    void createGenre(GenreRequestDTO genreRequestDTO);

    /**
     * Actualiza los datos de un género musical existente.
     *
     * @param id El identificador único del género musical a actualizar.
     * @param genreRequestDTO Un objeto {@link GenreRequestDTO} con los datos actualizados.
     */
    void updateGenre(Long id, GenreRequestDTO genreRequestDTO);

    /**
     * Elimina un género musical del sistema.
     *
     * @param id El identificador único del género musical a eliminar.
     */
    void deleteGenre(Long id);
}
