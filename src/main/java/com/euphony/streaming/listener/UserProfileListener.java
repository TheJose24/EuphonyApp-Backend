package com.euphony.streaming.listener;

import com.euphony.streaming.event.UserProfileDeletedEvent;
import com.euphony.streaming.exception.custom.user.UserDeletionException;
import com.euphony.streaming.util.KeycloakProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserProfileListener {

    @EventListener
    public void listenerUserProfileEvent(UserProfileDeletedEvent event) {
        log.info("Evento de eliminación de perfil de usuario recibido. ID del usuario: {}", event.getUserId());
        try {
            KeycloakProvider.getUsersResource().get(event.getUserId().toString()).remove();
            log.info("Usuario eliminado de Keycloak: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Error al eliminar el usuario en Keycloak: {}. Detalles: {}", event.getUserId(), e.getMessage());
            throw new UserDeletionException("Error al eliminar el usuario en Keycloak", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
