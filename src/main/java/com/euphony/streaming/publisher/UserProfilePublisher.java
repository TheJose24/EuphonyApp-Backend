package com.euphony.streaming.publisher;

import com.euphony.streaming.event.UserProfileDeletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class UserProfilePublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public UserProfilePublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishUserProfileDeleteEvent(UUID profileId) {
        log.info("Publicando evento de eliminación de perfil de usuario");
        applicationEventPublisher.publishEvent(new UserProfileDeletedEvent(profileId));
    }
}
