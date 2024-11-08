package com.euphony.streaming.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ARTISTA")
public class ArtistaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_artista")
    private Long idArtista;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "biografia")
    private String biografia;

    @Column(name = "pais", nullable = false, length = 100)
    private String pais;

    @Column(name = "redes_sociales", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> redesSociales;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;
}
