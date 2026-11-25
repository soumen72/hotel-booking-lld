package com.hbooking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class SearchResult {
    private final Property property;
    private final List<AvailableRoomOption> availableRooms;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AvailableRoomOption {
        private final RoomType roomType;
        private final Money totalPrice;
    }
}
