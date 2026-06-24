package com.euphony.streaming.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancionGeneroId implements Serializable {

    private Long cancion;
    private Long genero;
}
