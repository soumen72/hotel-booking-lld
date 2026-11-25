package com.hbooking.api;

import com.hbooking.dto.response.SearchResponse;
import com.hbooking.enums.Amenity;
import com.hbooking.model.DateRange;
import com.hbooking.model.GuestCount;
import com.hbooking.model.Money;
import com.hbooking.enums.StarRating;
import com.hbooking.model.SearchCriteria;
import com.hbooking.model.SearchResult;
import com.hbooking.service.SearchService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/properties/search")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<List<SearchResponse>> search(
            @RequestParam("city") String city,
            @RequestParam(value = "locality", required = false) String locality,
            @RequestParam("checkIn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam("checkOut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(value = "adults", defaultValue = "1") int adults,
            @RequestParam(value = "children", defaultValue = "0") int children,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "currency", defaultValue = "USD") String currency,
            @RequestParam(value = "amenities", required = false) List<String> amenities,
            @RequestParam(value = "minStarRating", required = false) Integer minStarRating) {

        DateRange dateRange = DateRange.of(checkIn, checkOut);
        GuestCount guestCount = GuestCount.of(adults, children);

        Money minMoney = minPrice != null ? Money.of(minPrice, currency) : null;
        Money maxMoney = maxPrice != null ? Money.of(maxPrice, currency) : null;

        Set<Amenity> amenitySet = new HashSet<>();
        if (amenities != null) {
            for (String a : amenities) {
                amenitySet.add(Amenity.valueOf(a.trim().toUpperCase()));
            }
        }

        StarRating starRating = minStarRating != null ? StarRating.fromValue(minStarRating) : null;

        SearchCriteria criteria = SearchCriteria.builder()
                .city(city)
                .locality(locality)
                .dateRange(dateRange)
                .guestCount(guestCount)
                .minPrice(minMoney)
                .maxPrice(maxMoney)
                .requiredAmenities(amenitySet)
                .minStarRating(starRating)
                .build();

        List<SearchResult> results = searchService.search(criteria);
        List<SearchResponse> response = results.stream()
                .map(SearchResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
