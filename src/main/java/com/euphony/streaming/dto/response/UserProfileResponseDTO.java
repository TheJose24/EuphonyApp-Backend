package com.euphony.streaming.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO de respuesta que contiene la información del perfil del usuario.")
public class UserProfileResponseDTO {

    @Schema(description = "ID único del perfil de usuario", example = "1")
    private Long idProfile;

    @Schema(description = "ID único del usuario", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID idUser;

    @Schema(description = "Nombre de usuario", example = "juanperez123")
    private String username;

    @Schema(description = "Correo electrónico del usuario", example = "juan.perez@example.com")
    private String email;

    @Schema(description = "Primer nombre del usuario", example = "Juan")
    private String firstName;

    @Schema(description = "Apellido del usuario", example = "Perez")
    private String lastName;

    @Schema(description = "Fecha de nacimiento del usuario", example = "1990-05-20")
    private String birthDate;

    @Schema(description = "URL o ruta de la imagen de perfil del usuario", example = "/imagenes/usuario.jpg")
    private String imgProfile;

    @Schema(description = "Número de teléfono del usuario", example = "+51 987654321")
    private String phone;

    @Schema(description = "País de residencia del usuario", example = "Perú")
    private String country;

    @Schema(description = "Ciudad de residencia del usuario", example = "Lima")
    private String city;
}
