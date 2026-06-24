package com.euphony.streaming.entity;

import com.euphony.streaming.audit.BaseAudit;
import com.euphony.streaming.util.enums.SubscriptionState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "SUSCRIPCION")
public class SuscripcionEntity extends BaseAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_suscripcion")
    private Long idSuscripcion;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    @ManyToOne
    @JoinColumn(name = "id_plan", nullable = false)
    private PlanesSuscripcionEntity plan;

    @ManyToOne
    @JoinColumn(name = "id_metodo_pago", nullable = false)
    private MetodoPagoEntity metodoPago;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_renovacion", nullable = false)
    private LocalDateTime fechaRenovacion;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @Column(name = "fecha_ultima_actualizacion")
    private LocalDateTime fechaUltimaActualizacion;

    @Column(name = "estado", nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionState estado = SubscriptionState.ACTIVE;

}
