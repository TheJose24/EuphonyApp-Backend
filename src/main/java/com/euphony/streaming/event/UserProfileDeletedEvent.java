package com.euphony.streaming.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserProfileDeletedEvent {

    private UUID userId;

}
