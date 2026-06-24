package com.euphony.streaming.service.interfaces;

import com.euphony.streaming.dto.request.SubscriptionRequestDTO;
import com.euphony.streaming.dto.response.SubscriptionResponseDTO;

import java.util.List;

/**
 * Interfaz que define las operaciones de gestión de suscripciones.
 */
public interface ISubscriptionService {

    /**
     * Crea una nueva suscripción.
     *
     * @param requestDTO Un objeto {@link SubscriptionRequestDTO} con los datos de la nueva suscripción.
     * @return El token de la suscripción creada.
     */
    String createSubscription(SubscriptionRequestDTO requestDTO);

    /**
     * Obtiene todas las suscripciones registradas.
     *
     * @return Lista de {@link SubscriptionResponseDTO} con la información de todas las suscripciones.
     */
    List<SubscriptionResponseDTO> getAllSubscriptions();

    /**
     * Obtiene información de una suscripción específica por su ID.
     *
     * @param id El identificador único de la suscripción.
     * @return Un {@link SubscriptionResponseDTO} con los datos de la suscripción.
     */
    SubscriptionResponseDTO getSubscriptionById(Long id);

    /**
     * Actualiza los datos de una suscripción existente.
     *
     * @param id El identificador único de la suscripción a actualizar.
     * @param requestDTO Un objeto {@link SubscriptionRequestDTO} con los datos actualizados.
     */
    void updateSubscription(Long id, SubscriptionRequestDTO requestDTO);

    /**
     * Elimina una suscripción del sistema.
     *
     * @param id El identificador único de la suscripción a eliminar.
     */
    void deleteSubscription(Long id);
}
