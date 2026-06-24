package com.euphony.streaming.service.interfaces;

import com.euphony.streaming.dto.request.PlansSubscriptionRequestDTO;
import com.euphony.streaming.dto.request.PlansUpdateRequestDTO;
import com.euphony.streaming.dto.response.PlansSubscriptionResponseDTO;

import java.util.List;

/**
 * Interfaz que define las operaciones de gestión de planes de suscripción.
 */
public interface IPlanesSuscripcionService {

    /**
     * Crea un nuevo plan de suscripción.
     *
     * @param requestDTO Un objeto {@link PlansSubscriptionRequestDTO} con los datos del nuevo plan.
     * @return El {@link PlansSubscriptionResponseDTO} del plan creado.
     */
    PlansSubscriptionResponseDTO createPlan(PlansSubscriptionRequestDTO requestDTO);

    /**
     * Obtiene todos los planes de suscripción registrados.
     *
     * @return Lista de {@link PlansSubscriptionResponseDTO} con la información de todos los planes.
     */
    List<PlansSubscriptionResponseDTO> getAllPlans();

    /**
     * Obtiene información de un plan específico por su ID.
     *
     * @param id El identificador único del plan.
     * @return Un {@link PlansSubscriptionResponseDTO} con los datos del plan.
     */
    PlansSubscriptionResponseDTO getPlanById(Long id);

    /**
     * Actualiza los datos de un plan existente.
     *
     * @param id El identificador único del plan a actualizar.
     * @param requestDTO Un objeto {@link PlansSubscriptionRequestDTO} con los datos actualizados.
     * @return El {@link PlansSubscriptionResponseDTO} con los datos del plan actualizado.
     */
    PlansSubscriptionResponseDTO updatePlan(Long id, PlansUpdateRequestDTO requestDTO);

    /**
     * Elimina un plan del sistema.
     *
     * @param id El identificador único del plan a eliminar.
     */
    void deletePlan(Long id);
}
