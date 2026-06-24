package com.euphony.streaming.service.implementation;

import com.euphony.streaming.dto.request.SubscriptionRequestDTO;
import com.euphony.streaming.dto.response.SubscriptionResponseDTO;
import com.euphony.streaming.entity.MetodoPagoEntity;
import com.euphony.streaming.entity.PlanesSuscripcionEntity;
import com.euphony.streaming.entity.SuscripcionEntity;
import com.euphony.streaming.entity.UsuarioEntity;
import com.euphony.streaming.exception.custom.subcriptionplans.InvalidPlanConfigurationException;
import com.euphony.streaming.exception.custom.subcriptionplans.SubscriptionPlansNotFoundException;
import com.euphony.streaming.exception.custom.subscription.SubscriptionNotFoundException;
import com.euphony.streaming.exception.custom.user.UserNotFoundException;
import com.euphony.streaming.repository.MetodoPagoRepository;
import com.euphony.streaming.repository.PlanesSuscripcionRepository;
import com.euphony.streaming.repository.SuscripcionRepository;
import com.euphony.streaming.repository.UsuarioRepository;
import com.euphony.streaming.service.interfaces.ISubscriptionService;
import com.euphony.streaming.util.enums.SubscriptionState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Validated
@RequiredArgsConstructor(onConstructor_ = @__(@Lazy))
@Transactional(readOnly = true)
public class SubscriptionServiceImpl implements ISubscriptionService {

    private final SuscripcionRepository suscripcionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlanesSuscripcionRepository planesSuscripcionRepository;
    private final MetodoPagoRepository metodoPagoRepository;

    @Override
    @Transactional
    public String createSubscription(SubscriptionRequestDTO requestDTO) {
        // Validar usuario
        UsuarioEntity usuario = usuarioRepository.findById(requestDTO.getIdUsuario())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: ID " + requestDTO.getIdUsuario(), HttpStatus.NOT_FOUND));

        // Validar plan
        PlanesSuscripcionEntity plan = planesSuscripcionRepository.findById(requestDTO.getIdPlan())
                .orElseThrow(() -> new SubscriptionPlansNotFoundException("Plan de suscripción no encontrado: ID " + requestDTO.getIdPlan(), HttpStatus.NOT_FOUND));

        // Validar metodo de pago
        MetodoPagoEntity metodoPago = metodoPagoRepository.findById(requestDTO.getIdMetodoPago())
                .orElseThrow(() -> new RuntimeException("Método de pago no encontrado: ID " + requestDTO.getIdMetodoPago()));

        // Persistir la suscripción en estado pendiente hasta su confirmación
        SuscripcionEntity suscripcion = new SuscripcionEntity();
        suscripcion.setUsuario(usuario);
        suscripcion.setPlan(plan);
        suscripcion.setMetodoPago(metodoPago);
        suscripcion.setFechaInicio(requestDTO.getFechaInicio() != null ? requestDTO.getFechaInicio() : LocalDateTime.now());
        suscripcion.setFechaRenovacion(requestDTO.getFechaRenovacion() != null ? requestDTO.getFechaRenovacion() : calculateNextBillingDate(plan));
        suscripcion.setEstado(SubscriptionState.PENDIG);

        suscripcionRepository.save(suscripcion);
        log.info("Suscripción creada exitosamente - ID: {}", suscripcion.getIdSuscripcion());
        return String.valueOf(suscripcion.getIdSuscripcion());
    }

    /**
     * Confirma/activa una suscripción previamente creada.
     *
     * @param idSuscripcion identificador de la suscripción a activar.
     */
    @Transactional
    public void executeSubscription(Long idSuscripcion) {
        log.debug("Activando suscripción con ID: {}", idSuscripcion);

        SuscripcionEntity subscription = suscripcionRepository.findById(idSuscripcion)
                .orElseThrow(() -> new SubscriptionNotFoundException("Suscripción no encontrada: ID " + idSuscripcion, HttpStatus.NOT_FOUND));

        subscription.setEstado(SubscriptionState.ACTIVE);
        subscription.setFechaInicio(LocalDateTime.now());
        subscription.setFechaRenovacion(calculateNextBillingDate(subscription.getPlan()));

        suscripcionRepository.save(subscription);
        log.info("Suscripción activada exitosamente - ID: {}", subscription.getIdSuscripcion());
    }

    private LocalDateTime calculateNextBillingDate(PlanesSuscripcionEntity plan) {
        LocalDateTime nextBilling = LocalDateTime.now();

        switch (plan.getIntervalo()) {
            case MONTH:
                return nextBilling.plusMonths(1);
            case YEAR:
                return nextBilling.plusYears(1);
            default:
                throw new InvalidPlanConfigurationException("Intervalo de facturación no soportado", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponseDTO> getAllSubscriptions() {
        return suscripcionRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponseDTO getSubscriptionById(Long id) {
        SuscripcionEntity suscripcion = suscripcionRepository.findById(id)
                .orElseThrow(() -> new SubscriptionNotFoundException("Suscripción no encontrada.", HttpStatus.NOT_FOUND));
        return mapToResponseDTO(suscripcion);
    }

    @Override
    @Transactional
    public void updateSubscription(Long id, SubscriptionRequestDTO requestDTO) {
        SuscripcionEntity suscripcion = suscripcionRepository.findById(id)
                .orElseThrow(() -> new SubscriptionNotFoundException("Suscripción no encontrada.", HttpStatus.NOT_FOUND));

        PlanesSuscripcionEntity plan = planesSuscripcionRepository.findById(requestDTO.getIdPlan())
                .orElseThrow(() -> new SubscriptionNotFoundException("Plan de suscripción no encontrado.", HttpStatus.NOT_FOUND));

        MetodoPagoEntity metodoPago = metodoPagoRepository.findById(requestDTO.getIdMetodoPago())
                .orElseThrow(() -> new SubscriptionNotFoundException("Método de pago no encontrado.", HttpStatus.NOT_FOUND));

        suscripcion.setPlan(plan);
        suscripcion.setMetodoPago(metodoPago);
        suscripcion.setFechaRenovacion(requestDTO.getFechaRenovacion());
        suscripcion.setEstado(SubscriptionState.valueOf(String.valueOf(requestDTO.getEstado())));

        suscripcionRepository.save(suscripcion);
    }

    @Override
    @Transactional
    public void deleteSubscription(Long id) {
        SuscripcionEntity suscripcion = suscripcionRepository.findById(id)
                .orElseThrow(() -> new SubscriptionNotFoundException("Suscripción no encontrada.", HttpStatus.NOT_FOUND));
        suscripcionRepository.delete(suscripcion);
    }

    private SubscriptionResponseDTO mapToResponseDTO(SuscripcionEntity entity) {
        SubscriptionResponseDTO dto = new SubscriptionResponseDTO();
        dto.setIdSuscripcion(entity.getIdSuscripcion());
        dto.setIdUsuario(entity.getUsuario().getIdUsuario());
        dto.setIdPlan(entity.getPlan().getIdPlan());
        dto.setIdMetodoPago(entity.getMetodoPago().getIdMetodoPago());
        dto.setFechaInicio(entity.getFechaInicio());
        dto.setFechaRenovacion(entity.getFechaRenovacion());
        dto.setFechaCancelacion(entity.getFechaCancelacion());
        dto.setEstado(entity.getEstado());
        return dto;
    }
}
