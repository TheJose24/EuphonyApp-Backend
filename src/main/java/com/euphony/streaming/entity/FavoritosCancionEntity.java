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
@Table(name = "FAVORITOS")
@IdClass(FavoritosCancionId.class)
public class FavoritosCancionEntity {

    @Id
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    @Id
    @ManyToOne
    @JoinColumn(name = "id_cancion", nullable = false)
    private CancionEntity cancion;

    @Column(name = "fecha_agregado", nullable = false)
    private LocalDateTime fechaAgregado;

}
