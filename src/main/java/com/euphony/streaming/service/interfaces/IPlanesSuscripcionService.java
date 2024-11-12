package com.euphony.streaming.service.interfaces;

import com.euphony.streaming.dto.request.PlanesSuscripcionRequestDTO;
import com.euphony.streaming.dto.response.PlanesSuscripcionResponseDTO;

import java.util.List;

/**
 * Interfaz que define las operaciones de gestión de planes de suscripción.
 */
public interface IPlanesSuscripcionService {

    /**
     * Crea un nuevo plan de suscripción.
     *
     * @param requestDTO Un objeto {@link PlanesSuscripcionRequestDTO} con los datos del nuevo plan.
     * @return El {@link PlanesSuscripcionResponseDTO} del plan creado.
     */
    PlanesSuscripcionResponseDTO createPlan(PlanesSuscripcionRequestDTO requestDTO);

    /**
     * Obtiene todos los planes de suscripción registrados.
     *
     * @return Lista de {@link PlanesSuscripcionResponseDTO} con la información de todos los planes.
     */
    List<PlanesSuscripcionResponseDTO> getAllPlans();

    /**
     * Obtiene información de un plan específico por su ID.
     *
     * @param id El identificador único del plan.
     * @return Un {@link PlanesSuscripcionResponseDTO} con los datos del plan.
     */
    PlanesSuscripcionResponseDTO getPlanById(Long id);

    /**
     * Actualiza los datos de un plan existente.
     *
     * @param id El identificador único del plan a actualizar.
     * @param requestDTO Un objeto {@link PlanesSuscripcionRequestDTO} con los datos actualizados.
     * @return El {@link PlanesSuscripcionResponseDTO} con los datos del plan actualizado.
     */
    PlanesSuscripcionResponseDTO updatePlan(Long id, PlanesSuscripcionRequestDTO requestDTO);

    /**
     * Elimina un plan del sistema.
     *
     * @param id El identificador único del plan a eliminar.
     */
    void deletePlan(Long id);
}
