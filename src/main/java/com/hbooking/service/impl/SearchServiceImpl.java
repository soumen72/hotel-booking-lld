package com.hbooking.service.impl;

import com.hbooking.model.Money;
import com.hbooking.model.Property;
import com.hbooking.model.RoomType;
import com.hbooking.policy.PricingStrategy;
import com.hbooking.repository.InventoryRepository;
import com.hbooking.repository.PropertyRepository;
import com.hbooking.model.SearchCriteria;
import com.hbooking.search.SearchFilter;
import com.hbooking.model.SearchResult;
import com.hbooking.service.SearchService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {
    private final PropertyRepository propertyRepository;
    private final InventoryRepository inventoryRepository;
    private final PricingStrategy pricingStrategy;
    private final List<SearchFilter> filters;

    public SearchServiceImpl(PropertyRepository propertyRepository,
                             InventoryRepository inventoryRepository,
                             PricingStrategy pricingStrategy,
                             List<SearchFilter> filters) {
        this.propertyRepository = propertyRepository;
        this.inventoryRepository = inventoryRepository;
        this.pricingStrategy = pricingStrategy;
        this.filters = filters != null ? filters : List.of();
    }

    @Override
    public List<SearchResult> search(SearchCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Search criteria cannot be null");
        }

        List<Property> matchedProperties = propertyRepository.findByLocation(
                criteria.getCity(), criteria.getLocality());

        List<SearchResult> results = new ArrayList<>();

        for (Property property : matchedProperties) {
            List<SearchResult.AvailableRoomOption> availableRoomOptions = new ArrayList<>();

            for (RoomType roomType : property.getRoomTypes().values()) {
                // Check capacity
                if (!roomType.canAccommodate(criteria.getGuestCount())) {
                    continue;
                }

                // Check real inventory availability for the entire date range
                if (!inventoryRepository.isAvailable(roomType.getId(), criteria.getDateRange(), 1)) {
                    continue;
                }

                // Calculate price for the stay
                Money calculatedPrice = pricingStrategy.calculatePrice(roomType, criteria.getDateRange());

                // Apply pluggable filter chain
                boolean matchesAllFilters = filters.stream()
                        .allMatch(filter -> filter.matches(property, roomType, calculatedPrice, criteria));

                if (matchesAllFilters) {
                    availableRoomOptions.add(SearchResult.AvailableRoomOption.builder()
                            .roomType(roomType)
                            .totalPrice(calculatedPrice)
                            .build());
                }
            }

            if (!availableRoomOptions.isEmpty()) {
                results.add(SearchResult.builder()
                        .property(property)
                        .availableRooms(availableRoomOptions)
                        .build());
            }
        }

        return results;
    }
}
