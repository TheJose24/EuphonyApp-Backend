package com.euphony.streaming.service.implementation;

import com.euphony.streaming.dto.request.UserRequestDTO;
import com.euphony.streaming.dto.response.UserResponseDTO;
import com.euphony.streaming.entity.PerfilUsuarioEntity;
import com.euphony.streaming.entity.RolEntity;
import com.euphony.streaming.entity.UsuarioEntity;
import com.euphony.streaming.exception.custom.UserCreationException;
import com.euphony.streaming.exception.custom.UserDeletionException;
import com.euphony.streaming.exception.custom.UserNotFoundException;
import com.euphony.streaming.exception.custom.UserUpdateException;
import com.euphony.streaming.repository.PerfilUsuarioRepository;
import com.euphony.streaming.repository.RolRepository;
import com.euphony.streaming.repository.UsuarioRepository;
import com.euphony.streaming.service.interfaces.IUserService;
import com.euphony.streaming.util.KeycloakProvider;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__(@Lazy))
public class UserServiceImpl implements IUserService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;

    private static final String CLIENT_ID = "euphony-client";

    @Override
    public UserResponseDTO getUser(UUID id) {
        return usuarioRepository.findById(id)
                .map(this::mapToUserResponseDTO)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
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
        // Validar que los campos del usuario no sean nulos ni vacíos.
        validateUserInput(userRequestDTO);

        UsersResource userResource = KeycloakProvider.getUsersResource();
        UserRepresentation userRepresentation = buildUserRepresentation(userRequestDTO);

        String userId = null;
        try (Response response = userResource.create(userRepresentation)) {
            int status = response.getStatus();

            if (status == 201) {
                // Usuario creado exitosamente en Keycloak
                userId = extractUserId(response);
                userRepresentation.setId(userId);

                // 1. Asignar contraseña al usuario en Keycloak
                setUserPassword(userId, userRequestDTO.getPassword());

                // 2. Asignar roles en Keycloak
                assignUserRoles(userId, userRequestDTO.getRoles());

                log.info("Usuario creado con éxito: {}", userRepresentation.getId());
                return UUID.fromString(userId);

            } else if (status == 409) {
                // Si el usuario ya existe en Keycloak, se lanza una excepción con código 409.
                log.error("El usuario ya existe: {}", userRequestDTO.getUsername());
                throw new UserCreationException("El usuario ya existe", HttpStatus.CONFLICT);
            } else {
                // Otro error al crear el usuario en Keycloak
                log.error("Error al crear el usuario: {}. Status: {}. Detalles: {}", userRequestDTO.getUsername(), status, response.getEntity());
                throw new UserCreationException("Error al crear el usuario, por favor contacte al administrador", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            // En caso de error, se hace rollback en Keycloak.
            log.error("Error en la creación del usuario: {}. Detalles: {}", userRequestDTO.getUsername(), e.getMessage());

            KeycloakProvider.getUsersResource()
                    .get(userId)
                    .remove();

            throw new UserCreationException("Error en la creación del usuario, por favor contacte al administrador", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @Override
    public void updateUser(@NotNull UserRequestDTO userRequestDTO, @NotNull UUID id) {
        // Validar la entrada del usuario
        validateUserInput(userRequestDTO);

        UsuarioEntity usuarioEntity = usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado en la base de datos: {}", id);
                    return new UserNotFoundException("Usuario no encontrado");
                });

        // Actualizar el usuario en Keycloak
        UsersResource userResource = KeycloakProvider.getUsersResource();
        UserRepresentation originalUserRepresentation;
        try {
            originalUserRepresentation = userResource.get(usuarioEntity.getIdUsuario().toString()).toRepresentation();
        } catch (Exception e) {
            log.error("Error al obtener los datos originales del usuario en Keycloak: {}. Detalles: {}", usuarioEntity.getIdUsuario(), e.getMessage());
            throw new UserUpdateException("Error al obtener los datos originales del usuario en Keycloak", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        UserRepresentation updatedUserRepresentation  = buildUserRepresentation(userRequestDTO);

        try {
            userResource.get(usuarioEntity.getIdUsuario().toString()).update(updatedUserRepresentation );
            log.info("Usuario actualizado en Keycloak: {}", usuarioEntity.getIdUsuario());
        } catch (Exception e) {
            log.error("Error al actualizar el usuario en Keycloak: {}. Detalles: {}", usuarioEntity.getIdUsuario(), e.getMessage());
            throw new UserUpdateException("Error al actualizar el usuario en Keycloak", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    @Transactional
    @Override
    public void deleteUser(UUID id) {
        // Validar que el ID no sea nulo
        if (id == null) {
            log.error("El ID del usuario es nulo.");
            throw new BadRequestException("El ID del usuario no puede ser nulo");
        }

        UsuarioEntity usuarioEntity = usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado en la base de datos: {}", id);
                    return new UserNotFoundException("Usuario no encontrado");
                });

        // Eliminar el usuario en Keycloak
        try {
            KeycloakProvider.getUsersResource().get(usuarioEntity.getIdUsuario().toString()).remove();
            log.info("Usuario eliminado de Keycloak: {}", usuarioEntity.getIdUsuario());
        } catch (Exception e) {
            log.error("Error al eliminar el usuario en Keycloak: {}. Detalles: {}", usuarioEntity.getIdUsuario(), e.getMessage());
            throw new UserDeletionException("Error al eliminar el usuario en Keycloak", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private void validateUserInput(UserRequestDTO userRequestDTO) {
        validateNotEmpty(userRequestDTO.getUsername(), "El nombre de usuario es obligatorio");
        validateNotEmpty(userRequestDTO.getEmail(), "El email es obligatorio");
        validateNotEmpty(userRequestDTO.getPassword(), "La contraseña es obligatoria");
    }

    private void validateNotEmpty(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new UserCreationException(errorMessage, HttpStatus.BAD_REQUEST);
        }
    }

    private String extractUserId(Response response) {
        String path = response.getLocation().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private void setUserPassword(String userId, String password) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(OAuth2Constants.PASSWORD);
        credentialRepresentation.setValue(password);

        KeycloakProvider.getUsersResource().get(userId).resetPassword(credentialRepresentation);
    }

    private void assignUserRoles(String userId, Set<String> roles) {
        RealmResource realmResource = KeycloakProvider.getRealmResource();
        String clientUuid = getClientUuid(realmResource);

        if (clientUuid == null) {
            log.error("No se encontró el cliente '{}' en Keycloak.", CLIENT_ID);
            return;
        }

        // Si no se proporcionan roles, asignar rol por defecto
        if (roles == null || roles.isEmpty()) {
            roles = Set.of("user_client_role"); // Rol por defecto
        }

        // Verificar y asignar roles en Keycloak
        List<RoleRepresentation> roleRepresentations = getRoleRepresentations(realmResource, clientUuid, roles);
        if (roleRepresentations.isEmpty()) {
            throw new UserCreationException("No se encontraron roles válidos en Keycloak.", HttpStatus.NOT_FOUND);
        }
        realmResource.users().get(userId).roles().clientLevel(clientUuid).add(roleRepresentations);

        // Verificar y asignar roles en la base de datos
        Set<RolEntity> roleEntities = roles.stream()
                .map(this::getOrCreateRoleEntity)  // Obtener o crear el rol en la base de datos
                .collect(Collectors.toSet());

        // Obtener el usuario desde la base de datos y asignar los roles
        UsuarioEntity usuarioEntity = usuarioRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en la base de datos"));
        usuarioEntity.setRoles(roleEntities);

        usuarioRepository.save(usuarioEntity);  // Guardar el usuario con los roles
    }

    private RolEntity getOrCreateRoleEntity(String roleName) {
        return rolRepository.findByNameRol(roleName).orElseGet(() -> {
            log.info("El rol '{}' no existe en la base de datos, se creará.", roleName);
            RolEntity newRole = new RolEntity();
            newRole.setNameRol(roleName);
            return rolRepository.save(newRole);  // Crear y guardar el rol
        });
    }

    private String getClientUuid(RealmResource realmResource) {
        var clients = realmResource.clients().findByClientId(CLIENT_ID);
        if (clients.isEmpty()) {
            log.error("El cliente '{}' no existe en el realm.", CLIENT_ID);
            return null;
        }
        return clients.get(0).getId();
    }

    private List<RoleRepresentation> getRoleRepresentations(RealmResource realmResource, String clientUuid, Set<String> roles) {
        return realmResource.clients().get(clientUuid).roles().list().stream()
                .filter(role -> roles.contains(role.getName()))
                .toList();
    }

    private UserRepresentation buildUserRepresentation(UserRequestDTO userRequestDTO) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(userRequestDTO.getFirstName());
        userRepresentation.setLastName(userRequestDTO.getLastName());
        userRepresentation.setEmail(userRequestDTO.getEmail());
        userRepresentation.setUsername(userRequestDTO.getUsername());
        userRepresentation.setEmailVerified(false);
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
}
