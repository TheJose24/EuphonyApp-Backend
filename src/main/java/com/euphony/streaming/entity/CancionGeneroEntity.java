package com.euphony.streaming.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "CANCION_GENERO")
@IdClass(CancionGeneroId.class)
public class CancionGeneroEntity {

    @Id
    @ManyToOne
    @JoinColumn(name = "id_cancion", nullable = false)
    private CancionEntity cancion;

    @Id
    @ManyToOne
    @JoinColumn(name = "id_genero", nullable = false)
    private GeneroEntity genero;
}
