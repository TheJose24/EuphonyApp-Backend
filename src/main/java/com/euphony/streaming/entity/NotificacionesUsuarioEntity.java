package com.euphony.streaming.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "NOTIFICACIONES_USUARIO")
public class NotificacionesUsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Long idNotificacion;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "mensaje", nullable = false)
    private String mensaje;

    @Column(name = "leido", nullable = false)
    private Boolean leido = false;

    @Column(name = "fecha_envio", nullable = false)
    @CreationTimestamp
    private LocalDateTime fechaEnvio;

}
