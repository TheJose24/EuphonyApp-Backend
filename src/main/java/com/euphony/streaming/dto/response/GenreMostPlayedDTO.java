package com.euphony.streaming.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenreMostPlayedDTO {
    private Long genreId;
    private String name;
    private Long totalPlays;
}