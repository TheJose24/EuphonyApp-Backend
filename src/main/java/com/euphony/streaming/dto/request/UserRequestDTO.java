package com.euphony.streaming.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de solicitud para la creación y actualización de usuarios.")
public class UserRequestDTO {

    @Schema(description = "Nombre de usuario único", example = "johndoe123")
    @NotBlank(message = "El nombre de usuario no debe estar vacío")
    @Size(min = 3, max = 20, message = "El nombre de usuario debe tener entre 3 y 20 caracteres")
    private String username;

    @Schema(description = "Correo electrónico del usuario", example = "john.doe@example.com")
    @Email(message = "Debe proporcionar un correo electrónico válido")
    @NotBlank(message = "El correo electrónico no debe estar vacío")
    private String email;

    @Schema(description = "Contraseña del usuario", example = "password123")
    @NotBlank(message = "La contraseña no debe estar vacía")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @Schema(description = "Nombre del usuario", example = "John")
    @NotBlank(message = "El nombre no debe estar vacío")
    private String firstName;

    @Schema(description = "Apellido del usuario", example = "Doe")
    @NotBlank(message = "El apellido no debe estar vacío")
    private String lastName;

    @Schema(description = "Roles asociados al usuario", example = "[\"user_client_role\", \"admin_client_role\", \"artist_client_role\"]")
    private Set<String> roles;

}
