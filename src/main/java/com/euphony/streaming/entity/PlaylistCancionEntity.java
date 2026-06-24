package com.euphony.streaming.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "PLAYLIST_CANCION")
@IdClass(PlaylistCancionId.class)
public class PlaylistCancionEntity {

    @Id
    @ManyToOne
    @JoinColumn(name = "id_playlist", nullable = false)
    private PlaylistEntity playlist;

    @Id
    @ManyToOne
    @JoinColumn(name = "id_cancion", nullable = false)
    private CancionEntity cancion;

}
