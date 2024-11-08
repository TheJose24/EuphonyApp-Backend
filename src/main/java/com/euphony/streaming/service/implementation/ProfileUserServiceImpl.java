package com.euphony.streaming.service.implementation;

import com.euphony.streaming.dto.request.UserProfileRequestDTO;
import com.euphony.streaming.dto.response.UserProfileResponseDTO;
import com.euphony.streaming.entity.PerfilUsuarioEntity;
import com.euphony.streaming.exception.custom.user.UserNotFoundException;
import com.euphony.streaming.exception.custom.user.UserUpdateException;
import com.euphony.streaming.publisher.UserProfilePublisher;
import com.euphony.streaming.repository.PerfilUsuarioRepository;
import com.euphony.streaming.service.interfaces.IProfileUserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__(@Lazy))
public class ProfileUserServiceImpl implements IProfileUserService {

    private final PerfilUsuarioRepository perfilUsuarioRepository;
    private final UserProfilePublisher userProfilePublisher;

    @Override
    public List<UserProfileResponseDTO> findAllProfiles() {
        return perfilUsuarioRepository.findAll().stream()
                .map(this::mapToUserProfileResponseDTO)
                .toList();
    }


    @Override
    public UserProfileResponseDTO searchProfileByUsuarioId(UUID usuarioId) {
        return perfilUsuarioRepository.findByUsuarioIdUsuario(usuarioId)
                .map(this::mapToUserProfileResponseDTO)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con id: " + usuarioId, HttpStatus.NOT_FOUND));
    }

    @Override
    public void deleteProfile(UUID profileId) {
        PerfilUsuarioEntity perfilUsuarioEntity = perfilUsuarioRepository.findByUsuarioIdUsuario(profileId)
                .orElseThrow(() -> new UserNotFoundException("Perfil de usuario no encontrado con id: " + profileId, HttpStatus.NOT_FOUND));

        if (perfilUsuarioEntity != null) {
            perfilUsuarioRepository.deleteById(perfilUsuarioEntity.getIdPerfil());
            log.info("Perfil con ID {} eliminado con éxito", profileId);

            userProfilePublisher.publishUserProfileDeleteEvent(perfilUsuarioEntity.getUsuario().getIdUsuario());
        }
    }

    @Override
    public void updateProfile(@NotNull UUID profileId, @NotNull UserProfileRequestDTO profileUsuarioRequestDTO) {
        try {
            PerfilUsuarioEntity perfil = perfilUsuarioRepository.findByUsuarioIdUsuario(profileId)
                    .orElseThrow(() -> {
                        log.error("Usuario con ID {} no encontrado", profileId);
                        return new UserNotFoundException("Usuario no encontrado con el ID proporcionado", HttpStatus.NOT_FOUND);
                    });

            // Actualizar los campos si son válidos
            updateIfNotNullOrEmpty(profileUsuarioRequestDTO.getBirthDate(), perfil::setFechaNacimiento);
            updateIfNotNullOrEmpty(profileUsuarioRequestDTO.getCountry(), perfil::setPais);
            updateIfNotNullOrEmpty(profileUsuarioRequestDTO.getImgProfile(), perfil::setImgPerfil);
            updateIfNotNullOrEmpty(profileUsuarioRequestDTO.getPhone(), perfil::setTelefono);
            updateIfNotNullOrEmpty(profileUsuarioRequestDTO.getCity(), perfil::setCiudad);

            perfilUsuarioRepository.save(perfil); // Guardar la entidad actualizada

            log.info("Perfil con ID {} actualizado con éxito", profileId);
        } catch (IllegalArgumentException e) {
            log.error("ID de perfil inválido: {}", profileId);
            throw new UserUpdateException("ID de perfil inválido", e, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error al actualizar el perfil de usuario con ID {}: {}", profileId, e.getMessage());
            throw new UserUpdateException("Error al actualizar el perfil de usuario", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private <T> void updateIfNotNullOrEmpty(T value, Consumer<T> setter) {
        if (value instanceof String string) {
            if (!string.trim().isEmpty()) {
                setter.accept(value);
            }
        } else if (value != null) {
            setter.accept(value);
        }
    }

    private UserProfileResponseDTO mapToUserProfileResponseDTO(PerfilUsuarioEntity perfilUsuarioEntity) {
        return UserProfileResponseDTO.builder()
                .idProfile(perfilUsuarioEntity.getIdPerfil())
                .idUser(perfilUsuarioEntity.getUsuario().getIdUsuario())
                .username(perfilUsuarioEntity.getUsuario().getUsername())
                .email(perfilUsuarioEntity.getUsuario().getEmail())
                .firstName(perfilUsuarioEntity.getUsuario().getNombre())
                .lastName(perfilUsuarioEntity.getUsuario().getApellido())
                .birthDate(String.valueOf(perfilUsuarioEntity.getFechaNacimiento()))
                .imgProfile(perfilUsuarioEntity.getImgPerfil())
                .phone(perfilUsuarioEntity.getTelefono())
                .country(perfilUsuarioEntity.getPais())
                .city(perfilUsuarioEntity.getCiudad())
                .build();
    }

}

