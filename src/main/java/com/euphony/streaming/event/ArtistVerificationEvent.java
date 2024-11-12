package com.euphony.streaming.event;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class ArtistVerificationEvent {

    private final Long artistId;
    private final String artistName;
    private final Long followersCount;

}
