package com.euphony.streaming.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeguidoresArtistaId implements Serializable {

    private UUID usuario;
    private Long artista;
}
