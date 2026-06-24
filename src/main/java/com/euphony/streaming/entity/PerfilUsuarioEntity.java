package com.euphony.streaming.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "PERFIL_USUARIO")
public class PerfilUsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil")
    private Long idPerfil;

    @OneToOne
    @JoinColumn(name = "id_usuario", unique = true, nullable = false)
    private UsuarioEntity usuario;

    @Column(name = "fecha_nacimiento")
    private Date fechaNacimiento;

    @Column(name = "pais", length = 100)
    private String pais;

    @Column(name = "img_perfil")
    private String imgPerfil;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "ciudad", length = 100)
    private String ciudad;

}
