package com.euphony.streaming.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "METODO_PAGO")
public class MetodoPagoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metodo_pago")
    private Long idMetodoPago;

    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @Column(name = "detalles")
    private String detalles;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
