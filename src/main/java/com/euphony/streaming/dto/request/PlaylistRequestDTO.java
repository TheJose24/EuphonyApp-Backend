package com.euphony.streaming.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para creación y actualización de lista de reproducción")
public class PlaylistRequestDTO {

    @Schema(description = "Nombre de la lista de reproducción", example = "Mi Lista de Rock")
    @NotBlank(message = "El nombre de la lista de reproducción no puede estar vacío")
    @Size(min = 3, max = 255, message = "El nombre debe tener entre 3 y 255 caracteres")
    private String name;

    @Schema(description = "Descripción de la lista de reproducción", example = "Colección de mis canciones favoritas")
    @Size(max = 1000, message = "La descripción no puede exceder los 1000 caracteres")
    private String description;

    @Schema(description = "Indica si la lista de reproducción es pública", example = "false", defaultValue = "false")
    @NotNull(message = "Debe especificar si la lista de reproducción es pública o privada")
    @Builder.Default
    private Boolean isPublic = false;

    @Schema(description = "URL de la imagen de portada", example = "https://example.com/image.jpg")
    @Size(max = 255, message = "La URL de la imagen no puede exceder los 255 caracteres")
    @Pattern(regexp = "^(https?://.*|)$", message = "La URL debe comenzar con http:// o https:// o estar vacía")
    private String coverImage;

    @Schema(description = "ID del usuario propietario", example = "123e4567-e89b-12d3-a456-426614174000")
    @NotNull(message = "El ID del usuario no puede ser nulo")
    private UUID userId;
}