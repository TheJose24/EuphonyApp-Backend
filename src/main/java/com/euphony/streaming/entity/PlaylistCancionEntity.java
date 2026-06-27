package com.euphony.streaming.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Relación canción↔playlist (tabla puente PLAYLIST_CANCION).
 * Clave primaria compuesta (id_playlist, id_cancion) mediante {@link PlaylistCancionId}.
 * {@code fechaAgregado} permite listar las canciones en el orden en que se añadieron.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "PLAYLIST_CANCION")
@IdClass(PlaylistCancionId.class)
@EntityListeners(AuditingEntityListener.class)
public class PlaylistCancionEntity {

    @Id
    @ManyToOne
    @JoinColumn(name = "id_playlist", nullable = false)
    private PlaylistEntity playlist;

    @Id
    @ManyToOne
    @JoinColumn(name = "id_cancion", nullable = false)
    private CancionEntity cancion;

    @Column(name = "fecha_agregado", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime fechaAgregado;
}
