package com.euphony.streaming.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Relación "me gusta" de un usuario sobre una canción (tabla puente FAVORITOS_CANCION).
 * Clave primaria compuesta (id_usuario, id_cancion) mediante {@link FavoritosCancionId}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "FAVORITOS_CANCION")
@IdClass(FavoritosCancionId.class)
@EntityListeners(AuditingEntityListener.class)
public class FavoritosCancionEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cancion", nullable = false)
    private CancionEntity cancion;

    @Column(name = "fecha_agregado", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime fechaAgregado;
}
