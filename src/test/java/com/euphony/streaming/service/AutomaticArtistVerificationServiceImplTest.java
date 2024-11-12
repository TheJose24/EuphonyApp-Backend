package com.euphony.streaming.service;

import com.euphony.streaming.entity.ArtistaEntity;
import com.euphony.streaming.publisher.ArtistVerificationPublisher;
import com.euphony.streaming.repository.ArtistaRepository;
import com.euphony.streaming.repository.SeguidoresArtistaRepository;
import com.euphony.streaming.service.implementation.AutomaticArtistVerificationServiceImpl;
import com.euphony.streaming.util.ArtistFollowerCount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class AutomaticArtistVerificationServiceImplTest {

    // Declaración de los mocks y la clase a probar
    @Mock
    private ArtistaRepository artistaRepository;

    @Mock
    private SeguidoresArtistaRepository seguidoresRepository;

    @Mock
    private ArtistVerificationPublisher artistVerificationPublisher;

    @InjectMocks
    private AutomaticArtistVerificationServiceImpl automaticArtistVerificationService;

    @BeforeEach
    void setUp() {
        // Inicializa los mocks antes de cada prueba
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void checkArtistsForVerification_verifiesEligibleArtists() {
        // Arrange
        // Crea un objeto ArtistFollowerCount simulado
        ArtistFollowerCount artistFollowerCount = new ArtistFollowerCount() {
            @Override
            public Long getIdArtista() {
                return 1L;
            }

            @Override
            public Long getFollowerCount() {
                return 100L;
            }
        };
        // Crea un objeto ArtistaEntity simulado
        ArtistaEntity artist = new ArtistaEntity();
        artist.setIdArtista(1L);
        artist.setNombre("Jose Jose");
        artist.setIsVerified(false);

        // Configura los comportamientos simulados de los métodos
        when(seguidoresRepository.findArtistsEligibleForVerification(anyLong())).thenReturn(List.of(artistFollowerCount));
        when(artistaRepository.findById(1L)).thenReturn(Optional.of(artist));

        // Act
        automaticArtistVerificationService.checkArtistsForVerification();

        // Assert
        // Verifica que se hayan llamado los métodos esperados
        verify(artistaRepository).save(artist);
        verify(artistVerificationPublisher).publishArtistVerification(1L, "Jose Jose", 100L);
    }

    @Test
    void checkArtistsForVerification_throwsExceptionWhenArtistNotFound() {
        // Arrange
        ArtistFollowerCount artistFollowerCount = new ArtistFollowerCount() {
            @Override
            public Long getIdArtista() {
                return 1L;
            }

            @Override
            public Long getFollowerCount() {
                return 100L;
            }
        };

        // Configura los comportamientos simulados de los métodos
        when(seguidoresRepository.findArtistsEligibleForVerification(anyLong())).thenReturn(List.of(artistFollowerCount));
        when(artistaRepository.findById(1L)).thenReturn(Optional.empty());

        // Assert
        // Verifica que se lance la excepción esperada
        assertThrows(RuntimeException.class, () -> automaticArtistVerificationService.checkArtistsForVerification());
    }

    @Test
    void checkArtistsForVerification_noEligibleArtists() {
        // Arrange
        // Configura los comportamientos simulados de los métodos
        when(seguidoresRepository.findArtistsEligibleForVerification(anyLong())).thenReturn(List.of());

        // Act
        automaticArtistVerificationService.checkArtistsForVerification();

        // Assert
        // Verifica que no se hayan llamado los métodos esperados
        verify(artistaRepository, never()).save(any());
        verify(artistVerificationPublisher, never()).publishArtistVerification(anyLong(), anyString(), anyLong());
    }
}