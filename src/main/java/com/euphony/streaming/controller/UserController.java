package com.euphony.streaming.controller;

import com.euphony.streaming.dto.request.UserRequestDTO;
import com.euphony.streaming.dto.response.UserResponseDTO;
import com.euphony.streaming.exception.custom.user.UserNotFoundException;
import com.euphony.streaming.service.implementation.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Gestión de Usuarios", description = "API para la gestión de usuarios")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__(@Lazy))
public class UserController {

    private final UserServiceImpl userService;

    @Operation(summary = "Obtener usuario por ID", description = "Recupera un usuario específico por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/search/{id}")
    public ResponseEntity<UserResponseDTO> getUser(
            @Parameter(description = "ID del usuario", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @Operation(summary = "Obtener todos los usuarios", description = "Recupera una lista de todos los usuarios")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios recuperada exitosamente",
            content = @Content(schema = @Schema(implementation = UserResponseDTO.class)))
    @GetMapping("/all")
    public ResponseEntity<List<UserResponseDTO>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @Operation(summary = "Crear un nuevo usuario", description = "Crea un nuevo usuario en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente",
                content = @Content(schema = @Schema(implementation = UUID.class))),
            @ApiResponse(responseCode = "409", description = "El username o el correo ya están en uso"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/create")
    public ResponseEntity<UUID> createUser(
            @Parameter(description = "Datos del usuario a crear", required = true)
            @RequestBody UserRequestDTO userRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userRequestDTO));
    }

    @Operation(summary = "Actualizar un usuario", description = "Actualiza los datos de un usuario existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<Void> updateUser(
            @Parameter(description = "ID del usuario a actualizar", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable("id") UUID id,
            @Parameter(description = "Nuevos datos del usuario", required = true)
            @RequestBody UserRequestDTO userRequestDTO) {
        try {
            userService.updateUser(userRequestDTO, id);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            log.error("Error al actualizar el usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error inesperado al actualizar el usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Eliminar un usuario", description = "Elimina un usuario existente del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID del usuario a eliminar", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable("id") UUID id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            log.error("Error al eliminar el usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error inesperado al eliminar el usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
