package com.euphony.streaming.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PlayHistoryResponseDTO {
    private Long historyId;
    private Long songId;
    private String songTitle;
    private String artistName;
    private String albumTitle;
    private LocalDateTime playedAt;
}
