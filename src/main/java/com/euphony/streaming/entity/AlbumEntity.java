package com.euphony.streaming.entity;

import com.euphony.streaming.audit.BaseAudit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ALBUM")
public class AlbumEntity extends BaseAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_album")
    private Long idAlbum;

    @ManyToOne
    @JoinColumn(name = "id_artista", nullable = false)
    private ArtistaEntity artista;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "fecha_lanzamiento", nullable = false)
    private LocalDate fechaLanzamiento;

    @Column(name = "portada")
    private String portada;
}
