package com.euphony.streaming.controller;

import com.euphony.streaming.dto.request.SubscriptionRequestDTO;
import com.euphony.streaming.dto.response.SubscriptionResponseDTO;
import com.euphony.streaming.entity.MetodoPagoEntity;
import com.euphony.streaming.entity.RolEntity;
import com.euphony.streaming.entity.UsuarioEntity;
import com.euphony.streaming.repository.MetodoPagoRepository;
import com.euphony.streaming.repository.UsuarioRepository;
import com.euphony.streaming.service.implementation.SubscriptionServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor(onConstructor_ = @__(@Lazy))
@Tag(name = "Gestión de Suscripciones", description = "API para la gestión de suscripciones")
@Slf4j
public class SubscriptionController {

    private final SubscriptionServiceImpl subscriptionService;
    private final UsuarioRepository usuarioRepository;
    private final MetodoPagoRepository metodoPagoRepository;

    @PostMapping("/create")
    public ResponseEntity<String> createSubscription(
            @RequestBody SubscriptionRequestDTO subscriptionRequestDTO) {
        String token = subscriptionService.createSubscription(subscriptionRequestDTO);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/execute/{id}")
    public ResponseEntity<Void> executeSubscription(@PathVariable Long id) {
        subscriptionService.executeSubscription(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/create-user")
    public ResponseEntity<Void> createUserSubscription(){
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(UUID.randomUUID());
        usuario.setNombre("Juan");
        usuario.setUsername("juanperez");
        usuario.setApellido("Perez");
        usuario.setEmail("juan@perez.com");
        usuario.setIsActive(true);
        usuarioRepository.save(usuario);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/create-payment-method")
    public ResponseEntity<Void> createPaymentMethod(){
        MetodoPagoEntity metodoPago = new MetodoPagoEntity();
        metodoPago.setTipo("Tarjeta de Crédito");
        metodoPago.setIsActive(true);
        metodoPago.setDetalles("Método de pago de prueba");
        metodoPagoRepository.save(metodoPago);
        return ResponseEntity.ok().build();
    }

}
