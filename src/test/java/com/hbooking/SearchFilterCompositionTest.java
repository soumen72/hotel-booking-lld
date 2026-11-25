package com.hbooking;

import com.hbooking.enums.Amenity;
import com.hbooking.model.DateRange;
import com.hbooking.model.GuestCount;
import com.hbooking.model.Location;
import com.hbooking.model.Money;
import com.hbooking.model.Property;
import com.hbooking.enums.RoomCategory;
import com.hbooking.model.RoomType;
import com.hbooking.enums.StarRating;
import com.hbooking.policy.impl.DefaultPricingStrategy;
import com.hbooking.repository.impl.InMemoryInventoryRepository;
import com.hbooking.repository.impl.InMemoryPropertyRepository;
import com.hbooking.model.SearchCriteria;
import com.hbooking.search.SearchFilter;
import com.hbooking.model.SearchResult;
import com.hbooking.search.impl.AmenityFilter;
import com.hbooking.search.impl.PriceRangeFilter;
import com.hbooking.search.impl.StarRatingFilter;
import com.hbooking.service.SearchService;
import com.hbooking.service.impl.SearchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchFilterCompositionTest {
    private SearchService searchService;
    private InMemoryInventoryRepository inventoryRepo;

    @BeforeEach
    void setUp() {
        var propertyRepo = new InMemoryPropertyRepository();
        inventoryRepo = new InMemoryInventoryRepository();

        List<SearchFilter> filters = List.of(
                new PriceRangeFilter(),
                new AmenityFilter(),
                new StarRatingFilter()
        );

        searchService = new SearchServiceImpl(propertyRepo, inventoryRepo, new DefaultPricingStrategy(), filters);

        // Property 1: 5-Star Luxury Resort with POOL, WIFI, SPA in Chicago
        Property p1 = Property.builder()
                .id("P1")
                .ownerId("OWN-1")
                .name("Grand Palace")
                .location(Location.builder().city("Chicago").locality("Downtown").address("100 Michigan Ave").build())
                .starRating(StarRating.FIVE_STAR)
                .amenities(Set.of(Amenity.SWIMMING_POOL, Amenity.WIFI, Amenity.SPA))
                .build();
        RoomType p1r1 = RoomType.builder()
                .id("RT1")
                .propertyId("P1")
                .category(RoomCategory.SUITE)
                .name("King Suite")
                .basePricePerNight(Money.of(300.0, "USD"))
                .maxCapacity(3)
                .totalInventory(5)
                .amenities(Set.of())
                .build();
        p1.addRoomType(p1r1);
        propertyRepo.save(p1);
        inventoryRepo.initializeInventory("RT1", 5);

        // Property 2: 3-Star Budget Hotel with WIFI only in Chicago
        Property p2 = Property.builder()
                .id("P2")
                .ownerId("OWN-2")
                .name("Budget Inn")
                .location(Location.builder().city("Chicago").locality("Airport").address("456 Runway Rd").build())
                .starRating(StarRating.THREE_STAR)
                .amenities(Set.of(Amenity.WIFI))
                .build();
        RoomType p2r1 = RoomType.builder()
                .id("RT2")
                .propertyId("P2")
                .category(RoomCategory.STANDARD)
                .name("Standard Room")
                .basePricePerNight(Money.of(100.0, "USD"))
                .maxCapacity(2)
                .totalInventory(5)
                .amenities(Set.of())
                .build();
        p2.addRoomType(p2r1);
        propertyRepo.save(p2);
        inventoryRepo.initializeInventory("RT2", 5);

        // Property 3: 4-Star Hotel in New York (different city)
        Property p3 = Property.builder()
                .id("P3")
                .ownerId("OWN-3")
                .name("Manhattan Suites")
                .location(Location.builder().city("New York").locality("Manhattan").address("789 5th Ave").build())
                .starRating(StarRating.FOUR_STAR)
                .amenities(Set.of(Amenity.WIFI, Amenity.GYM))
                .build();
        RoomType p3r1 = RoomType.builder()
                .id("RT3")
                .propertyId("P3")
                .category(RoomCategory.STUDIO)
                .name("Studio")
                .basePricePerNight(Money.of(200.0, "USD"))
                .maxCapacity(2)
                .totalInventory(5)
                .amenities(Set.of())
                .build();
        p3.addRoomType(p3r1);
        propertyRepo.save(p3);
        inventoryRepo.initializeInventory("RT3", 5);
    }

    @Test
    void testSearchByCityOnly() {
        DateRange dateRange = DateRange.of(LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        SearchCriteria criteria = SearchCriteria.builder()
                .city("Chicago")
                .dateRange(dateRange)
                .guestCount(GuestCount.of(2))
                .build();

        List<SearchResult> results = searchService.search(criteria);
        // Both P1 and P2 are in Chicago
        assertEquals(2, results.size());
    }

    @Test
    void testFilterByStarRating() {
        DateRange dateRange = DateRange.of(LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        SearchCriteria criteria = SearchCriteria.builder()
                .city("Chicago")
                .dateRange(dateRange)
                .guestCount(GuestCount.of(2))
                .minStarRating(StarRating.FOUR_STAR)
                .build();

        List<SearchResult> results = searchService.search(criteria);
        // Only P1 is 4-star or above (it's 5-star)
        assertEquals(1, results.size());
        assertEquals("P1", results.get(0).getProperty().getId());
    }

    @Test
    void testFilterByPriceRange() {
        DateRange dateRange = DateRange.of(LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)); // 2 nights
        // Total price for 2 nights: P1 = $600, P2 = $200
        SearchCriteria criteria = SearchCriteria.builder()
                .city("Chicago")
                .dateRange(dateRange)
                .guestCount(GuestCount.of(2))
                .minPrice(Money.of(150.0, "USD"))
                .maxPrice(Money.of(250.0, "USD"))
                .build();

        List<SearchResult> results = searchService.search(criteria);
        // Only P2 matches total price of $200
        assertEquals(1, results.size());
        assertEquals("P2", results.get(0).getProperty().getId());
    }

    @Test
    void testFilterByAmenity() {
        DateRange dateRange = DateRange.of(LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        SearchCriteria criteria = SearchCriteria.builder()
                .city("Chicago")
                .dateRange(dateRange)
                .guestCount(GuestCount.of(2))
                .requiredAmenities(Set.of(Amenity.SWIMMING_POOL))
                .build();

        List<SearchResult> results = searchService.search(criteria);
        // Only P1 has SWIMMING_POOL
        assertEquals(1, results.size());
        assertEquals("P1", results.get(0).getProperty().getId());
    }

    @Test
    void testComposedFilters() {
        DateRange dateRange = DateRange.of(LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        // Criteria: Chicago + 5-Star + SWIMMING_POOL + max budget $1000
        SearchCriteria criteria = SearchCriteria.builder()
                .city("Chicago")
                .dateRange(dateRange)
                .guestCount(GuestCount.of(2))
                .maxPrice(Money.of(1000.0, "USD"))
                .requiredAmenities(Set.of(Amenity.SWIMMING_POOL))
                .minStarRating(StarRating.FIVE_STAR)
                .build();

        List<SearchResult> results = searchService.search(criteria);
        assertEquals(1, results.size());
        assertEquals("P1", results.get(0).getProperty().getId());

        // Now lower max budget to $500 (P1 is $600) -> should yield 0 results
        SearchCriteria tightBudget = SearchCriteria.builder()
                .city("Chicago")
                .dateRange(dateRange)
                .guestCount(GuestCount.of(2))
                .maxPrice(Money.of(500.0, "USD"))
                .requiredAmenities(Set.of(Amenity.SWIMMING_POOL))
                .minStarRating(StarRating.FIVE_STAR)
                .build();
        assertTrue(searchService.search(tightBudget).isEmpty());
    }

    @Test
    void testSoldOutInventoryExcludedFromSearch() {
        DateRange dateRange = DateRange.of(LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        // Deplete all 5 rooms of P2
        inventoryRepo.reserveInventory("RT2", dateRange, 5);

        SearchCriteria criteria = SearchCriteria.builder()
                .city("Chicago")
                .dateRange(dateRange)
                .guestCount(GuestCount.of(2))
                .build();

        List<SearchResult> results = searchService.search(criteria);
        // Only P1 has availability
        assertEquals(1, results.size());
        assertEquals("P1", results.get(0).getProperty().getId());
    }
}
