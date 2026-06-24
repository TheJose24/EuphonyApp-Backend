package com.euphony.streaming.dto.response;

import lombok.Data;
import lombok.Builder;

@Data
@Builder

public class SongInPlaylistResponseDTO {
    private Long songId;
    private String title;
    private String artist;
    private String album;
    private String duration;
    private String coverArt;
}
