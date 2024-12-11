package com.euphony.streaming.controller;

import com.euphony.streaming.dto.request.UserProfileRequestDTO;
import com.euphony.streaming.dto.response.UserProfileResponseDTO;
import com.euphony.streaming.service.implementation.ProfileUserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/profile")
@Tag(name = "Gestión de perfil de usuario", description = "API para la gestión de perfil de usuario")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__(@Lazy))
public class ProfileUserController {

    private final ProfileUserServiceImpl perfilUsuarioService;

    @GetMapping("/all")
    @Operation(summary = "Listar perfiles de usuario")
    @ApiResponse(responseCode = "200", description = "Listar todos los perfiles de usuario", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserProfileResponseDTO.class)))
    public ResponseEntity<List<UserProfileResponseDTO>> getAllProfileUsers() {
        List<UserProfileResponseDTO> perfiles = perfilUsuarioService.findAllProfiles();
        return ResponseEntity.ok(perfiles);
    }

    @GetMapping("/search/{userId}")
    @Operation(summary = "Buscar perfil por ID de usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserProfileResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Perfil no encontrado")
    })
    public ResponseEntity<UserProfileResponseDTO> getProfileUser(@PathVariable UUID userId) {
        UserProfileResponseDTO perfil = perfilUsuarioService.searchProfileByUsuarioId(userId);
        return ResponseEntity.ok(perfil);
    }

    @DeleteMapping("/delete/{userId}")
    @Operation(summary = "Eliminar perfil por ID de usuario")
    @ApiResponse(responseCode = "204", description = "Perfil eliminado con éxito")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        perfilUsuarioService.deleteProfile(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{userId}")
    @Operation(summary = "Actualizar perfil por ID de usuario")
    @ApiResponse(responseCode = "204", description = "Perfil actualizado con éxito")
    public ResponseEntity<Void> updateUser(
            @PathVariable UUID userId,
            @RequestBody UserProfileRequestDTO userProfileRequestDTO) {
        perfilUsuarioService.updateProfile(userId, userProfileRequestDTO);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile-image/{userId}")
    @Operation(summary = "Obtener imagen de perfil")
    @ApiResponse(responseCode = "200", description = "Imagen de perfil", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))
    public ResponseEntity<String> getProfileImage(@PathVariable UUID userId) {
        String imgPerfil = perfilUsuarioService.SearchImageProfileByUsuarioId(userId);
        return ResponseEntity.ok(imgPerfil);
    }
}
