package com.euphony.streaming.dto.response;

import com.euphony.streaming.entity.RolEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de respuesta que contiene la información del usuario.")
public class UserResponseDTO {

    @Schema(description = "Identificador único del usuario", example = "123e4567-e89b-12d3-a456-556642440000")
    private UUID idUsuario;

    @Schema(description = "Correo electrónico del usuario", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Nombre de usuario", example = "johndoe123")
    private String username;

    @Schema(description = "Nombre del usuario", example = "John")
    private String firstName;

    @Schema(description = "Apellido del usuario", example = "Doe")
    private String lastName;

    @Schema(description = "Indica si el usuario está activo", example = "true")
    private Boolean isActive;

    @Schema(description = "Roles asignados al usuario")
    private Set<RolEntity> roles;

    /*
    @Schema(description = "Fecha de nacimiento del usuario", example = "1990-01-01")
    private String fechaNacimiento;

    @Schema(description = "País de residencia del usuario", example = "Perú")
    private String pais;

    @Schema(description = "URL de la imagen de perfil del usuario", example = "https://example.com/profile.jpg")
    private String imgPerfil;

    @Schema(description = "Teléfono del usuario", example = "+51987654321")
    private String telefono;

    @Schema(description = "Ciudad de residencia del usuario", example = "Lima")
    private String ciudad;
    */
}
