package com.hbooking.dto.response;

import com.hbooking.model.SearchResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class SearchResponse {
    private final String propertyId;
    private final String propertyName;
    private final String city;
    private final String locality;
    private final int starRating;
    private final List<AvailableRoomDto> availableRooms;

    public SearchResponse(SearchResult result) {
        this.propertyId = result.getProperty().getId();
        this.propertyName = result.getProperty().getName();
        this.city = result.getProperty().getLocation().getCity();
        this.locality = result.getProperty().getLocation().getLocality();
        this.starRating = result.getProperty().getStarRating().getValue();
        this.availableRooms = result.getAvailableRooms().stream()
                .map(r -> AvailableRoomDto.builder()
                        .roomTypeId(r.getRoomType().getId())
                        .category(r.getRoomType().getCategory() != null ? r.getRoomType().getCategory().name() : null)
                        .roomTypeName(r.getRoomType().getName())
                        .maxCapacity(r.getRoomType().getMaxCapacity())
                        .totalPrice(r.getTotalPrice().toString())
                        .build())
                .collect(Collectors.toList());
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailableRoomDto {
        private String roomTypeId;
        private String category;
        private String roomTypeName;
        private int maxCapacity;
        private String totalPrice;
    }
}
