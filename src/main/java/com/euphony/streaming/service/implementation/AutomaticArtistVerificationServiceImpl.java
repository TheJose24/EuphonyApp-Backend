package com.euphony.streaming.service.implementation;

import com.euphony.streaming.entity.ArtistaEntity;
import com.euphony.streaming.publisher.ArtistVerificationPublisher;
import com.euphony.streaming.repository.ArtistaRepository;
import com.euphony.streaming.repository.SeguidoresArtistaRepository;
import com.euphony.streaming.service.interfaces.IAutomaticArtistVerificationService;
import com.euphony.streaming.util.ArtistFollowerCount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__(@Lazy))
public class AutomaticArtistVerificationServiceImpl implements IAutomaticArtistVerificationService {

    private final ArtistaRepository artistaRepository;
    private final SeguidoresArtistaRepository seguidoresRepository;
    private final ArtistVerificationPublisher artistVerificationPublisher;
    private static final int MIN_FOLLOWERS_FOR_AUTO_VERIFICATION = 1000;

    @Override
    @Scheduled(cron = "0 0 0 * * ?") // Se ejecuta todos los días a medianoche
    @Transactional
    public void checkArtistsForVerification() {
        log.info("Iniciando verificación automática de artistas...");

        // Obtener solo los artistas no verificados que cumplen el criterio de seguidores
        List<ArtistFollowerCount> eligibleArtists = seguidoresRepository.findArtistsEligibleForVerification((long) MIN_FOLLOWERS_FOR_AUTO_VERIFICATION);

        eligibleArtists.forEach(artistFollowerCount -> {
            Long artistId = artistFollowerCount.getIdArtista();
            Long followersCount = artistFollowerCount.getFollowerCount();

            // Marcar el artista como verificado
            ArtistaEntity artist = artistaRepository.findById(artistId)
                    .orElseThrow(() -> new RuntimeException("Artista no encontrado con ID: " + artistId));
            artist.setIsVerified(true);
            artist.setVerificationReason("Verificación automática por alcanzar " + followersCount + " seguidores");

            artistaRepository.save(artist);

            // Publicar evento de verificación
            artistVerificationPublisher.publishArtistVerification(artistId, artist.getNombre(), followersCount);

            log.info("Artista verificado automáticamente: {} con {} seguidores", artist.getNombre(), followersCount);
        });

        log.info("Proceso de verificación automática completado");
    }

}
