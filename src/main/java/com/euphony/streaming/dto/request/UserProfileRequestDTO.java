package com.euphony.streaming.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de solicitud para la actualización de perfiles de usuarios.")
public class UserProfileRequestDTO {

    @Schema(description = "Fecha de nacimiento del usuario", example = "1990-01-01")
    private Date birthDate;

    @Schema(description = "País de residencia del usuario", example = "Perú")
    private String country;

    @Schema(description = "URL o ruta de la imagen de perfil del usuario", example = "/imagenes/usuario.jpg")
    private String imgProfile;

    @Schema(description = "Número de teléfono del usuario", example = "+51 987654321")
    private String phone;

    @Schema(description = "Ciudad de residencia del usuario", example = "Lima")
    private String city;

}
