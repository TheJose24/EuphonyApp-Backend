package com.euphony.streaming.service.implementation;

import com.euphony.streaming.dto.request.PlansSubscriptionRequestDTO;
import com.euphony.streaming.dto.request.PlansUpdateRequestDTO;
import com.euphony.streaming.dto.response.PlansSubscriptionResponseDTO;
import com.euphony.streaming.entity.PlanesSuscripcionEntity;
import com.euphony.streaming.exception.custom.subcriptionplans.InvalidPlanConfigurationException;
import com.euphony.streaming.exception.custom.subcriptionplans.SubscriptionPlansCreationException;
import com.euphony.streaming.exception.custom.subcriptionplans.SubscriptionPlansDeletionException;
import com.euphony.streaming.exception.custom.subcriptionplans.SubscriptionPlansNotFoundException;
import com.euphony.streaming.repository.PlanesSuscripcionRepository;
import com.euphony.streaming.service.interfaces.IPlanesSuscripcionService;
import com.euphony.streaming.util.enums.SubscriptionFeature;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
@Validated
@RequiredArgsConstructor(onConstructor_ = @__(@Lazy))
@Transactional(readOnly = true)
public class PlansSubscriptionServiceImpl implements IPlanesSuscripcionService {

    private static final String PLAN_NOT_FOUND_MESSAGE = "Plan no encontrado con ID: %d";
    private static final String PLAN_DELETION_ERROR = "Error al eliminar el plan";

    private final PlanesSuscripcionRepository planesSuscripcionRepository;

    @Override
    @Transactional
    @CacheEvict(value = "planes", allEntries = true)
    public PlansSubscriptionResponseDTO createPlan(@Valid PlansSubscriptionRequestDTO requestDTO) {
        log.debug("Iniciando creación de plan de suscripción: {}", requestDTO.getPlanName());
        log.trace("Detalles del request: {}", requestDTO);

        validatePlanCreation(requestDTO);
        checkDuplicatePlanName(requestDTO.getPlanName());
        validatePlanFeatures(requestDTO.getFeatures());

        PlanesSuscripcionEntity newPlan = mapToEntity(requestDTO);
        log.debug("Entidad plan mapeada: {}", newPlan);

        PlanesSuscripcionEntity savedPlan = planesSuscripcionRepository.save(newPlan);
        log.info("Plan de suscripción creado exitosamente - ID: {}", savedPlan.getIdPlan());

        return mapToResponseDTO(savedPlan);
    }

    private void validatePlanFeatures(Set<SubscriptionFeature> features) {
        if (features == null || features.isEmpty()) {
            throw new InvalidPlanConfigurationException("El plan debe tener al menos una característica", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public List<PlansSubscriptionResponseDTO> getAllPlans() {
        log.debug("Obteniendo todos los planes de suscripción");
        return planesSuscripcionRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    public PlansSubscriptionResponseDTO getPlanById(Long id) {
        log.debug("Buscando plan con ID: {}", id);
        return planesSuscripcionRepository.findById(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new SubscriptionPlansNotFoundException(
                        String.format(PLAN_NOT_FOUND_MESSAGE, id),
                        HttpStatus.NOT_FOUND
                ));
    }

    @Override
    @Transactional
    public PlansSubscriptionResponseDTO updatePlan(Long id, @Valid PlansUpdateRequestDTO requestDTO) {
        log.debug("Iniciando actualización del plan de suscripción con ID: {}", id);

        // Verificar existencia del plan
        PlanesSuscripcionEntity existingPlan = planesSuscripcionRepository.findById(id)
                .orElseThrow(() -> new SubscriptionPlansNotFoundException(
                        String.format(PLAN_NOT_FOUND_MESSAGE, id),
                        HttpStatus.NOT_FOUND
                ));

        // Actualizar los campos del plan con los valores del DTO
        updatePlanFields(existingPlan, requestDTO);

        // Guardar los cambios en la base de datos
        PlanesSuscripcionEntity updatedPlan = planesSuscripcionRepository.save(existingPlan);

        log.info("Plan actualizado exitosamente. ID: {}, Nombre: {}", updatedPlan.getIdPlan(), updatedPlan.getNombrePlan());
        return mapToResponseDTO(updatedPlan);
    }

    @Override
    @Transactional
    public void deletePlan(Long id) {
        log.debug("Iniciando eliminación del plan con ID: {}", id);

        // Verificar existencia del plan
        PlanesSuscripcionEntity plan = planesSuscripcionRepository.findById(id)
                .orElseThrow(() -> new SubscriptionPlansNotFoundException(
                        String.format(PLAN_NOT_FOUND_MESSAGE, id),
                        HttpStatus.NOT_FOUND
                ));

        try {
            planesSuscripcionRepository.delete(plan);
            log.info("Plan eliminado exitosamente. ID: {}", id);
        } catch (Exception ex) {
            log.error("Error al eliminar el plan con ID: {}", id, ex);
            throw new SubscriptionPlansDeletionException(PLAN_DELETION_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validatePlanCreation(PlansSubscriptionRequestDTO requestDTO) {
        if (Objects.isNull(requestDTO.getInterval())) {
            throw new SubscriptionPlansCreationException(
                    "El intervalo de facturación es obligatorio",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (Objects.isNull(requestDTO.getPlanName()) || requestDTO.getPlanName().trim().isEmpty()) {
            throw new SubscriptionPlansCreationException(
                    "El nombre del plan es obligatorio",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (Objects.isNull(requestDTO.getPrice()) || requestDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SubscriptionPlansCreationException(
                    "El precio debe ser mayor a 0",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (Objects.isNull(requestDTO.getDuration()) || requestDTO.getDuration() <= 0) {
            throw new SubscriptionPlansCreationException(
                    "La duración debe ser mayor a 0",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (Objects.isNull(requestDTO.getDescription()) || requestDTO.getDescription().trim().isEmpty()) {
            throw new SubscriptionPlansCreationException(
                    "La descripción es obligatoria",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void checkDuplicatePlanName(String planName) {
        if (Boolean.TRUE.equals(planesSuscripcionRepository.existsByNombrePlan(planName))) {
            throw new SubscriptionPlansCreationException(
                    "Ya existe un plan con este nombre",
                    HttpStatus.CONFLICT
            );
        }
    }

    /**
     * Metodo auxiliar para actualizar los campos del plan existente con los valores del DTO.
     */
    private void updatePlanFields(PlanesSuscripcionEntity existingPlan, PlansUpdateRequestDTO requestDTO) {
        log.trace("Actualizando campos del plan con datos del DTO: {}", requestDTO);

        if (requestDTO.getPlanName() != null && !requestDTO.getPlanName().trim().isEmpty()) {
            existingPlan.setNombrePlan(requestDTO.getPlanName());
        }
        if (requestDTO.getDescription() != null && !requestDTO.getDescription().trim().isEmpty()) {
            existingPlan.setDescripcion(requestDTO.getDescription());
        }
        if (requestDTO.getSetupFee() != null && requestDTO.getSetupFee().compareTo(BigDecimal.ZERO) >= 0) {
            existingPlan.setTarifaInicial(requestDTO.getSetupFee());
        }

        log.debug("Campos actualizados para el plan con ID: {}", existingPlan.getIdPlan());
    }

    private PlanesSuscripcionEntity mapToEntity(PlansSubscriptionRequestDTO dto) {

        PlanesSuscripcionEntity entity = new PlanesSuscripcionEntity();
        entity.setIntervalo(dto.getInterval());
        entity.setFuncionalidades(dto.getFeatures());
        entity.setTarifaInicial(dto.getSetupFee());
        entity.setNombrePlan(dto.getPlanName());
        entity.setPrecio(dto.getPrice());
        entity.setDuracion(dto.getDuration());
        entity.setDescripcion(dto.getDescription());
        entity.setFreeTrial(dto.getFreeTrial());
        entity.setDiasPrueba(dto.getDurationFreeTrial());
        return entity;
    }

    private PlansSubscriptionResponseDTO mapToResponseDTO(PlanesSuscripcionEntity entity) {
        return PlansSubscriptionResponseDTO.builder()
                .planId(entity.getIdPlan())
                .interval(entity.getIntervalo())
                .features(entity.getFuncionalidades())
                .setupFee(entity.getTarifaInicial())
                .planName(entity.getNombrePlan())
                .price(entity.getPrecio())
                .duration(entity.getDuracion())
                .description(entity.getDescripcion())
                .isActive(entity.getIsActive())
                .freeTrial(entity.getFreeTrial())
                .durationFreeTrial(entity.getDiasPrueba())
                .build();
    }
}
