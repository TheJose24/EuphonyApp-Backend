package com.euphony.streaming.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para respuestas de operaciones con seguidores")
public class FollowersArtistResponseDTO {

    @Schema(description = "ID del usuario", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @Schema(description = "Nombre del usuario", example = "John Doe")
    private String userName;

    @Schema(description = "ID del artista", example = "1")
    private Long artistId;

    @Schema(description = "Nombre del artista", example = "Taylor Swift")
    private String artistName;

    @Schema(description = "Fecha cuando se comenzó a seguir al artista",
            example = "2024-03-20T15:30:00")
    private LocalDateTime followDate;
}
