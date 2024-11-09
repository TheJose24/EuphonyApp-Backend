package com.euphony.streaming.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para solicitudes de seguir/dejar de seguir artistas")
public class FollowersArtistRequestDTO {

    @NotNull(message = "El ID del usuario no puede ser nulo")
    @Schema(description = "ID del usuario que realiza la acción", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @NotNull(message = "El ID del artista no puede ser nulo")
    @Schema(description = "ID del artista objetivo", example = "1")
    private Long artistId;

}
