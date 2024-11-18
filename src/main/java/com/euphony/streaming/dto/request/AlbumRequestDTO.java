package com.euphony.streaming.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de solicitud para creación y actualización álbumes")
public class AlbumRequestDTO {

    @Schema(description = "ID del artista", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idArtist;

    @Schema(description = "Título del álbum", example = "The Dark Side of the Moon", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Fecha de lanzamiento del álbum", example = "1973-03-01")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    @Schema(description = "Portada del álbum", example = "https://upload.wikimedia.org/wikipedia/en/3/3b/Dark_Side_of_the_Moon.png")
    private String cover;
}
