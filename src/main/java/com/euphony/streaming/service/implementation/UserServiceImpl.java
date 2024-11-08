package com.euphony.streaming.service.implementation;

import com.euphony.streaming.dto.request.UserRequestDTO;
import com.euphony.streaming.dto.response.UserResponseDTO;
import com.euphony.streaming.entity.RolEntity;
import com.euphony.streaming.entity.UsuarioEntity;
import com.euphony.streaming.exception.custom.user.UserCreationException;
import com.euphony.streaming.exception.custom.user.UserNotFoundException;
import com.euphony.streaming.repository.RolRepository;
import com.euphony.streaming.repository.UsuarioRepository;
import com.euphony.streaming.service.interfaces.IUserService;
import com.euphony.streaming.util.KeycloakProvider;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__(@Lazy))
public class UserServiceImpl implements IUserService {

    private static final String CLIENT_ID = "euphony-client";
    private static final String USER_NOT_FOUND = "Usuario no encontrado";
    private static final String CLIENT_NOT_FOUND = "Cliente no encontrado en Keycloak";
    private static final String INVALID_ROLES = "Roles no válidos en Keycloak";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    @Override
    public UserResponseDTO getUser(UUID id) {
        return usuarioRepository.findById(id)
                .map(this::mapToUserResponseDTO)
                .orElseThrow(this::createUserNotFoundException);
    }

    @Override
    public List<UserResponseDTO> getUsers() {
        return usuarioRepository.findAll().stream()
                .map(this::mapToUserResponseDTO)
                .toList();
    }

    @Transactional
    @Override
    public UUID createUser(@NotNull UserRequestDTO userRequestDTO) {
        validateUserInput(userRequestDTO);

        UserRepresentation userRepresentation = createUserRepresentation(userRequestDTO);
        String userId = createKeycloakUser(userRepresentation);

        try {
            configureUserPassword(userId, userRequestDTO.getPassword());
            assignUserRoles(userId, userRequestDTO.getRoles());

            log.info("Usuario creado exitosamente en Keycloak con ID: {}", userId);
            return UUID.fromString(userId);
        } catch (Exception e) {
            // Si algo falla durante la configuración, eliminamos el usuario creado
            deleteKeycloakUser(userId);
            throw new UserCreationException("Error durante la configuración del usuario: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @Override
    public void updateUser(@NotNull UserRequestDTO userRequestDTO, @NotNull UUID id) {
        UsuarioEntity usuario = findUserById(id);

        UserRepresentation userRepresentation = createUserRepresentation(userRequestDTO);
        userRepresentation.setId(usuario.getIdUsuario().toString());

        try {
            UserResource userResource = KeycloakProvider.getUsersResource().get(userRepresentation.getId());

            // Actualizar el usuario en Keycloak
            userResource.update(userRepresentation);

            if (userRequestDTO.getRoles() != null && !userRequestDTO.getRoles().isEmpty()) {
                assignUserRoles(userRepresentation.getId(), userRequestDTO.getRoles());
            } else {
                log.info("Roles no proporcionados; se mantienen los roles actuales.");

            }
            log.info("Usuario actualizado en Keycloak: {}", id);
        } catch (Exception e) {
            log.error("Error al actualizar usuario en Keycloak: {}", e.getMessage());
            throw new UserCreationException("Error al actualizar usuario: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @Override
    public void deleteUser(UUID id) {
        UsuarioEntity usuario = findUserById(id);
        deleteKeycloakUser(usuario.getIdUsuario().toString());
        log.info("Usuario eliminado completamente: {}", id);
    }

    private void validateUserInput(UserRequestDTO userRequestDTO) {
        log.info(userRequestDTO.toString());
        log.info("Validando campos de usuario");
        if (isAnyFieldEmpty(userRequestDTO)) {
            throw new UserCreationException("Campos de usuario incompletos", HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isAnyFieldEmpty(UserRequestDTO userRequestDTO) {
        log.info("Comprobando campos vacíos");
        log.info(userRequestDTO.toString());
        return Stream.of(
                userRequestDTO.getUsername(),
                userRequestDTO.getEmail(),
                userRequestDTO.getPassword()
        ).anyMatch(this::isEmpty);
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String createKeycloakUser(UserRepresentation userRepresentation) {
        try (Response response = KeycloakProvider.getUsersResource().create(userRepresentation)) {
            if (response.getStatus() == HttpStatus.CREATED.value()) {
                return extractUserId(response);
            }
            throw new UserCreationException(
                    "Error al crear usuario: " + response.getStatusInfo().getReasonPhrase(),
                    HttpStatus.valueOf(response.getStatus())
            );
        }
    }

    private String extractUserId(Response response) {
        String path = response.getLocation().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private void configureUserPassword(String userId, String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);

        KeycloakProvider.getUsersResource()
                .get(userId)
                .resetPassword(credential);
    }

    private void assignUserRoles(String userId, Set<String> roles) {
        RealmResource realmResource = KeycloakProvider.getRealmResource();
        String clientUuid = findClientUuid(realmResource);

        List<RoleRepresentation> roleRepresentations = findRoleRepresentations(realmResource, clientUuid, roles);
        validateRoles(roleRepresentations);

        assignRolesToUser(realmResource, userId, clientUuid, roleRepresentations);
        saveUserRolesInDatabase(userId, roles);
    }

    private String findClientUuid(RealmResource realmResource) {
        return realmResource.clients().findByClientId(CLIENT_ID)
                .stream()
                .findFirst()
                .map(ClientRepresentation::getId)
                .orElseThrow(() -> new UserCreationException(CLIENT_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private List<RoleRepresentation> findRoleRepresentations(
            RealmResource realmResource,
            String clientUuid,
            Set<String> roles) {
        return realmResource.clients()
                .get(clientUuid)
                .roles()
                .list()
                .stream()
                .filter(role -> roles.contains(role.getName()))
                .toList();
    }

    private void validateRoles(List<RoleRepresentation> roleRepresentations) {
        if (roleRepresentations.isEmpty()) {
            throw new UserCreationException(INVALID_ROLES, HttpStatus.NOT_FOUND);
        }
    }

    private void assignRolesToUser(
            RealmResource realmResource,
            String userId,
            String clientUuid,
            List<RoleRepresentation> roleRepresentations) {
        try {
            realmResource.users()
                    .get(userId)
                    .roles()
                    .clientLevel(clientUuid)
                    .add(roleRepresentations);
        } catch (Exception e) {
            log.error("Error al asignar roles al usuario: {}", e.getMessage());
            throw new UserCreationException("Error al asignar roles: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void saveUserRolesInDatabase(String userId, Set<String> roles) {
        UsuarioEntity usuario = findUserById(UUID.fromString(userId));
        Set<RolEntity> roleEntities = roles.stream()
                .map(this::getOrCreateRoleEntity)
                .collect(Collectors.toSet());

        usuario.setRoles(roleEntities);
        usuarioRepository.save(usuario);
    }

    private RolEntity getOrCreateRoleEntity(String roleName) {
        return rolRepository.findByNameRol(roleName)
                .orElseGet(() -> createNewRole(roleName));
    }

    private RolEntity createNewRole(String roleName) {
        log.info("Creando nuevo rol en base de datos: {}", roleName);
        RolEntity newRole = new RolEntity();
        newRole.setNameRol(roleName);
        return rolRepository.save(newRole);
    }

    private UserRepresentation createUserRepresentation(UserRequestDTO userRequestDTO) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(userRequestDTO.getFirstName());
        userRepresentation.setLastName(userRequestDTO.getLastName());
        userRepresentation.setEmail(userRequestDTO.getEmail());
        userRepresentation.setUsername(userRequestDTO.getUsername());
        userRepresentation.setEnabled(true);
        return userRepresentation;
    }

    private UserResponseDTO mapToUserResponseDTO(UsuarioEntity usuario) {
        return UserResponseDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .firstName(usuario.getNombre())
                .lastName(usuario.getApellido())
                .isActive(usuario.getIsActive())
                .roles(usuario.getRoles())
                .build();
    }

    private UsuarioEntity findUserById(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(this::createUserNotFoundException);
    }

    private UserNotFoundException createUserNotFoundException() {
        return new UserNotFoundException(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    private void deleteKeycloakUser(String userId) {
        try {
            KeycloakProvider.getUsersResource()
                    .get(userId)
                    .remove();
            log.info("Usuario eliminado de Keycloak: {}", userId);
        } catch (Exception e) {
            log.error("Error al eliminar usuario de Keycloak: {}", e.getMessage());
            throw new UserCreationException("Error al eliminar usuario: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}