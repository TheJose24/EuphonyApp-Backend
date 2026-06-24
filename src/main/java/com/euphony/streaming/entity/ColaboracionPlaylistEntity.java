package com.euphony.streaming.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "COLABORACION_PLAYLIST")
@IdClass(ColaboracionPlaylistId.class)
public class ColaboracionPlaylistEntity {

    @Id
    @ManyToOne
    @JoinColumn(name = "id_playlist", nullable = false)
    private PlaylistEntity playlist;

    @Id
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    @Column(name = "fecha_colaboracion", nullable = false)
    private LocalDateTime fechaColaboracion;
}
