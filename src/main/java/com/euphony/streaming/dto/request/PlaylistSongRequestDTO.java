package com.euphony.streaming.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para añadir una canción a una playlist")
public class PlaylistSongRequestDTO {

    @NotNull(message = "El ID de la canción no puede ser nulo")
    @Schema(description = "ID de la canción a añadir", example = "10")
    private Long songId;
}
