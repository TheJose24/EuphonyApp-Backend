package com.euphony.streaming.entity;

import com.euphony.streaming.audit.BaseAudit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "BOLETA")
public class BoletaEntity extends BaseAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_boleta")
    private Long idBoleta;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    @ManyToOne
    @JoinColumn(name = "id_suscripcion", nullable = true)
    private SuscripcionEntity suscripcion;

    @ManyToOne
    @JoinColumn(name = "id_metodo_pago", nullable = false)
    private MetodoPagoEntity metodoPago;

    @Column(name = "numero_boleta", unique = true, nullable = false, length = 20)
    private String numeroBoleta;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @Column(name = "monto_total", nullable = false)
    private BigDecimal montoTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoBoleta estado = EstadoBoleta.PENDIENTE;

    public enum EstadoBoleta {
        PAGADA, PENDIENTE, ANULADA
    }
}
