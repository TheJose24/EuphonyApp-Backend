package com.euphony.streaming.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "CANCION")
public class CancionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cancion")
    private Long idCancion;

    @ManyToOne
    @JoinColumn(name = "id_artista", nullable = false)
    private ArtistaEntity artista;

    @ManyToOne
    @JoinColumn(name = "id_album")
    private AlbumEntity album;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "portada")
    private String portada;

    @Column(name = "duracion", nullable = false)
    private String duracion;

    @Column(name = "idioma", nullable = false, length = 50)
    private String idioma;

    @Column(name = "letra")
    private String letra;

    @Column(name = "fecha_lanzamiento", nullable = false)
    private String anioLanzamiento;

    @Column(name = "ruta_archivo", unique = true, nullable = false, length = 255)
    private String rutaArchivo;

    @Column(name = "calificacion_promedio", precision = 3, scale = 2)
    private BigDecimal calificacionPromedio;

    @Column(name = "numero_reproducciones", nullable = false)
    private Integer numeroReproducciones = 0;
}
