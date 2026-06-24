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
@Table(name = "CALIFICACIONES")
@IdClass(CalificacionesId.class)
public class CalificacionesEntity {

    @Id
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    @Id
    @ManyToOne
    @JoinColumn(name = "id_cancion", nullable = false)
    private CancionEntity cancion;

    @Column(name = "calificacion", nullable = false)
    private Integer calificacion;

    @Column(name = "fecha_calificacion", nullable = false)
    private LocalDateTime fechaCalificacion;

}
