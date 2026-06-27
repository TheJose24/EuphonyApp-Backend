package com.euphony.streaming.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Relación "álbum favorito" de un usuario (tabla puente FAVORITOS_ALBUM).
 * Clave primaria compuesta (id_usuario, id_album) mediante {@link FavoritosAlbumId}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "FAVORITOS_ALBUM")
@IdClass(FavoritosAlbumId.class)
@EntityListeners(AuditingEntityListener.class)
public class FavoritosAlbumEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_album", nullable = false)
    private AlbumEntity album;

    @Column(name = "fecha_agregado", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime fechaAgregado;
}
