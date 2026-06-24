package com.euphony.streaming.service.interfaces;

import com.euphony.streaming.dto.request.UserRequestDTO;
import com.euphony.streaming.dto.response.UserResponseDTO;

import java.util.List;
import java.util.UUID;

/**
 * Interfaz que define las operaciones relacionadas con la gestión de usuarios.
 */
public interface IUserService {

    /**
     * Obtiene la información de un usuario específico a partir de su ID.
     *
     * @param id El identificador único del usuario.
     * @return Un {@link UserResponseDTO} con los datos del usuario.
     */
    UserResponseDTO getUser(UUID id);

    /**
     * Obtiene una lista de todos los usuarios registrados en el sistema.
     *
     * @return Una lista de {@link UserResponseDTO} que contiene los datos de todos los usuarios.
     */
    List<UserResponseDTO> getUsers();

    /**
     * Crea un nuevo usuario en el sistema a partir de los datos proporcionados.
     *
     * @param userRequestDTO Un objeto {@link UserRequestDTO} que contiene los datos del nuevo usuario.
     * @return El UUID generado del nuevo usuario creado.
     */
    UUID createUser(UserRequestDTO userRequestDTO);

    /**
     * Actualiza los datos de un usuario existente a partir de su ID.
     *
     * @param userRequestDTO Un objeto {@link UserRequestDTO} con los datos actualizados del usuario.
     * @param id El identificador único del usuario que se desea actualizar.
     */
    void updateUser(UserRequestDTO userRequestDTO, UUID id);

    /**
     * Elimina un usuario del sistema a partir de su ID.
     *
     * @param id El identificador único del usuario que se desea eliminar.
     */
    void deleteUser(UUID id);
}
