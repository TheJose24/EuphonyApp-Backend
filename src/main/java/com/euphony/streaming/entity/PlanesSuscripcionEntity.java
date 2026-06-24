package com.euphony.streaming.entity;

import com.euphony.streaming.util.enums.PlanInterval;
import com.euphony.streaming.util.enums.SubscriptionFeature;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "PLANES_SUSCRIPCION")
@EntityListeners(AuditingEntityListener.class)
public class PlanesSuscripcionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plan")
    private Long idPlan;

    @Column(name = "intervalo", nullable = false)
    @Enumerated(EnumType.STRING)
    private PlanInterval intervalo;

    @Column(name = "funcionalidades", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Set<SubscriptionFeature> funcionalidades;

    @Column(name = "tarifa_inicial", precision = 10, scale = 2)
    private BigDecimal tarifaInicial;

    @Column(name = "nombre_plan", nullable = false, unique = true, length = 100)
    private String nombrePlan;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "duracion", nullable = false)
    private Integer duracion;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "free_trial", nullable = false)
    private Boolean freeTrial;

    @Column(name = "dias_prueba")
    private Integer diasPrueba;

}
