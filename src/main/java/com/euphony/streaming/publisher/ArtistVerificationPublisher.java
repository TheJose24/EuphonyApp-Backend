package com.euphony.streaming.publisher;

import com.euphony.streaming.event.ArtistVerificationEvent;
import com.euphony.streaming.event.UserProfileDeletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class ArtistVerificationPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;


    public ArtistVerificationPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishArtistVerification(Long artistId, String artistName, Long followersCount) {
        log.info("Publicando evento de verificación de artista");
        applicationEventPublisher.publishEvent(new ArtistVerificationEvent(artistId, artistName, followersCount));
    }
}
