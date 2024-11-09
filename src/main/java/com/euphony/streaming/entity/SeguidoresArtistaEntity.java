package com.euphony.streaming.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "SEGUIDORES_ARTISTA")
@IdClass(SeguidoresArtistaId.class)
public class SeguidoresArtistaEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_artista", nullable = false)
    private ArtistaEntity artista;

    @Column(name = "fecha_seguimiento", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime fechaSeguimiento;

}
