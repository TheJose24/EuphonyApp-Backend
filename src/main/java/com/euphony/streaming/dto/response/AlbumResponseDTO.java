package com.euphony.streaming.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de respuesta que contiene la información de un álbum")
public class AlbumResponseDTO {

        @Schema(description = "Identificador del álbum", example = "1")
        private Long idAlbum;

        @Schema(description = "Artista del álbum")
        private ArtistResponseDTO artista;

        @Schema(description = "Título del álbum", example = "The Dark Side of the Moon")
        private String titulo;

        @Schema(description = "Fecha de lanzamiento del álbum", example = "1973-03-01")
        private String fechaLanzamiento;

        @Schema(description = "URL de la portada del álbum", example = "https://upload.wikimedia.org/wikipedia/en/3/3b/Dark_Side_of_the_Moon.png")
        private String portada;
}
