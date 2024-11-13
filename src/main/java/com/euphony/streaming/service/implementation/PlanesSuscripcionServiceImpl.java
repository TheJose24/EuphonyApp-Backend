package com.euphony.streaming.service.implementation;

import com.euphony.streaming.dto.request.PlanesSuscripcionRequestDTO;
import com.euphony.streaming.dto.response.PlanesSuscripcionResponseDTO;
import com.euphony.streaming.entity.PlanesSuscripcionEntity;
import com.euphony.streaming.exception.custom.subcriptionplans.SubscriptionPlansCreationException;
import com.euphony.streaming.exception.custom.subcriptionplans.SubscriptionPlansDeletionException;
import com.euphony.streaming.exception.custom.subcriptionplans.SubscriptionPlansNotFoundException;
import com.euphony.streaming.repository.PlanesSuscripcionRepository;
import com.euphony.streaming.service.interfaces.IPlanesSuscripcionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.lang.reflect.Field;



import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__(@Lazy))
public class PlanesSuscripcionServiceImpl implements IPlanesSuscripcionService {

    private final PlanesSuscripcionRepository repository;

    @Override
    public PlanesSuscripcionResponseDTO createPlan(PlanesSuscripcionRequestDTO requestDTO) {
        log.info("Intentando crear un nuevo plan de suscripción...");

        validarCamposParaCreacion(requestDTO);
        verificarNombreDuplicado(requestDTO.getPlanName());

        PlanesSuscripcionEntity entity = mapToEntity(requestDTO);
        PlanesSuscripcionEntity savedEntity = repository.save(entity);

        log.info("Plan creado exitosamente: {}", savedEntity.getNombrePlan());
        return mapToResponseDTO(savedEntity);
    }

    @Override
    public List<PlanesSuscripcionResponseDTO> getAllPlans() {
        log.info("Obteniendo todos los planes de suscripción...");
        return repository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PlanesSuscripcionResponseDTO getPlanById(Long id) {
        log.info("Buscando plan con ID: {}", id);
        PlanesSuscripcionEntity entity = repository.findById(id)
                .orElseThrow(() -> new SubscriptionPlansNotFoundException("Plan no encontrado con ID: " + id, HttpStatus.NOT_FOUND));
        return mapToResponseDTO(entity);
    }


    @Transactional
    @Override
    public PlanesSuscripcionResponseDTO updatePlan(Long id, PlanesSuscripcionRequestDTO requestDTO) {
        log.info("Actualizando plan de suscripción con ID: {}", id);

        // Buscar la entidad por ID
        PlanesSuscripcionEntity entity = repository.findById(id)
                .orElseThrow(() -> new SubscriptionPlansNotFoundException("Plan no encontrado con ID: " + id, HttpStatus.NOT_FOUND));

        // Actualizar los campos de la entidad solo si el valor en el DTO no es nulo
        for (Field field : requestDTO.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true); // Hacer accesible el campo privado
                Object value = field.get(requestDTO); // Obtener el valor del campo en el DTO
                if (value != null) { // Si el valor no es nulo, actualiza el campo correspondiente en la entidad
                    Field entityField = entity.getClass().getDeclaredField(field.getName()); // Obtener el campo correspondiente en la entidad
                    entityField.setAccessible(true);
                    entityField.set(entity, value); // Asignar el valor a la entidad
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.warn("No se pudo actualizar el campo: " + field.getName(), e);
            }
        }

        // Guardar la entidad actualizada
        PlanesSuscripcionEntity updatedEntity = repository.save(entity);
        log.info("Plan actualizado exitosamente: {}", updatedEntity.getNombrePlan());

        // Retornar la respuesta con el DTO mapeado
        return mapToResponseDTO(updatedEntity);
    }




    @Override
    public void deletePlan(Long id) {
        log.info("Eliminando plan con ID: {}", id);

        PlanesSuscripcionEntity entity = repository.findById(id)
                .orElseThrow(() -> new SubscriptionPlansNotFoundException("Plan no encontrado con ID: " + id, HttpStatus.NOT_FOUND));
        try {
            repository.delete(entity);
            log.info("Plan eliminado exitosamente.");
        } catch (Exception ex) {
            log.error("Error al eliminar el plan con ID: {}", id, ex);
            throw new SubscriptionPlansDeletionException("Error al eliminar el plan", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validarCamposParaCreacion(PlanesSuscripcionRequestDTO requestDTO) {
        if (requestDTO.getPlanName() == null || requestDTO.getPlanName().isEmpty()) {
            throw new SubscriptionPlansCreationException("El campo 'planName' es obligatorio.", HttpStatus.BAD_REQUEST);
        }

        if (requestDTO.getPrice() == null || requestDTO.getPrice() <= 0) {
            throw new SubscriptionPlansCreationException("El campo 'price' es obligatorio y debe ser mayor a 0.", HttpStatus.BAD_REQUEST);
        }

        if (requestDTO.getDuration() == null || requestDTO.getDuration() <= 0) {
            throw new SubscriptionPlansCreationException("El campo 'duration' es obligatorio y debe ser mayor a 0.", HttpStatus.BAD_REQUEST);
        }

        if (requestDTO.getDescription() == null || requestDTO.getDescription().isEmpty()) {
            throw new SubscriptionPlansCreationException("El campo 'description' es obligatorio.", HttpStatus.BAD_REQUEST);
        }
    }

    private void verificarNombreDuplicado(String planName) {
        List<PlanesSuscripcionEntity> existingPlans = repository.findAll();
        boolean exists = existingPlans.stream()
                .anyMatch(plan -> plan.getNombrePlan().equalsIgnoreCase(planName));
        if (exists) {
            throw new SubscriptionPlansCreationException("El plan ya existe.", HttpStatus.CONFLICT);
        }
    }

    private PlanesSuscripcionEntity mapToEntity(PlanesSuscripcionRequestDTO dto) {
        PlanesSuscripcionEntity entity = new PlanesSuscripcionEntity();
        entity.setNombrePlan(dto.getPlanName());
        entity.setPrecio(dto.getPrice());
        entity.setDuracion(dto.getDuration());
        entity.setDescripcion(dto.getDescription());
        entity.setIsActive(dto.getIsActive());
        return entity;
    }

    private PlanesSuscripcionResponseDTO mapToResponseDTO(PlanesSuscripcionEntity entity) {
        return PlanesSuscripcionResponseDTO.builder()
                .planId(entity.getIdPlan())
                .planName(entity.getNombrePlan())
                .price(entity.getPrecio())
                .duration(entity.getDuracion())
                .description(entity.getDescripcion())
                .isActive(entity.getIsActive())
                .build();
    }
}
