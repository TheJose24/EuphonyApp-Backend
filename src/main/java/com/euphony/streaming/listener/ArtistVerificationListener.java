package com.euphony.streaming.listener;

import com.euphony.streaming.entity.ArtistaEntity;
import com.euphony.streaming.entity.NotificacionesArtistaEntity;
import com.euphony.streaming.event.ArtistVerificationEvent;
import com.euphony.streaming.exception.custom.user.UserNotFoundException;
import com.euphony.streaming.repository.ArtistaRepository;
import com.euphony.streaming.repository.NotificacionesArtistaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class ArtistVerificationListener {

    private final NotificacionesArtistaRepository notificacionesArtistaRepository;
    private final ArtistaRepository artistaRepository;

    @EventListener
    public void handleArtistVerification(ArtistVerificationEvent event) {
        log.info("Manejando evento de verificación para el artista: {}", event.getArtistName());

        // Crear notificación para el artista
        NotificacionesArtistaEntity notificacion = new NotificacionesArtistaEntity();
        ArtistaEntity artista = artistaRepository.findById(event.getArtistId())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + event.getArtistId(), HttpStatus.NOT_FOUND));

        notificacion.setArtista(artista); // Asumiendo que el ID del artista corresponde al usuario
        notificacion.setTitulo("¡Felicitaciones! Tu cuenta ha sido verificada");
        notificacion.setMensaje("Tu cuenta ha sido verificada automáticamente por alcanzar " +
                event.getFollowersCount() + " seguidores. ¡Sigue creando música!");

        notificacionesArtistaRepository.save(notificacion);

        log.info("Notificación de verificación creada para el artista: {}", event.getArtistName());
    }
}