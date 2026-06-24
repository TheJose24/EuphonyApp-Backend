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
@Table(name = "FAVORITOS_ALBUM")
@IdClass(FavoritosAlbumId.class)
public class FavoritosAlbumEntity {

    @Id
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    @Id
    @ManyToOne
    @JoinColumn(name = "id_album", nullable = false)
    private AlbumEntity album;

    @Column(name = "fecha_agregado", nullable = false)
    private LocalDateTime fechaAgregado;
}
