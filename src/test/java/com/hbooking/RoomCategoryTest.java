package com.hbooking;

import com.hbooking.model.Money;
import com.hbooking.enums.RoomCategory;
import com.hbooking.model.RoomType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RoomCategoryTest {

    @Test
    @DisplayName("Should validate room capacity correctly against category limits")
    void shouldValidateCapacity() {
        assertTrue(RoomCategory.STANDARD.isValidCapacity(1));
        assertTrue(RoomCategory.STANDARD.isValidCapacity(2));
        assertTrue(RoomCategory.STANDARD.isValidCapacity(4));
        assertFalse(RoomCategory.STANDARD.isValidCapacity(0));
        assertFalse(RoomCategory.STANDARD.isValidCapacity(5));

        assertTrue(RoomCategory.FAMILY_SUITE.isValidCapacity(2));
        assertTrue(RoomCategory.FAMILY_SUITE.isValidCapacity(8));
        assertFalse(RoomCategory.FAMILY_SUITE.isValidCapacity(1));
        assertFalse(RoomCategory.FAMILY_SUITE.isValidCapacity(9));
    }

    @Test
    @DisplayName("Should resolve category from exact or partial strings")
    void shouldResolveCategoryFromString() {
        assertEquals(RoomCategory.STANDARD, RoomCategory.fromString("STANDARD"));
        assertEquals(RoomCategory.STANDARD, RoomCategory.fromString("standard"));
        assertEquals(RoomCategory.STANDARD, RoomCategory.fromString("Standard Room"));
        assertEquals(RoomCategory.DELUXE, RoomCategory.fromString("Deluxe Room"));
        assertEquals(RoomCategory.DELUXE, RoomCategory.fromString("DELUXE"));
        assertEquals(RoomCategory.SUITE, RoomCategory.fromString("King Suite"));
        assertEquals(RoomCategory.SUITE, RoomCategory.fromString("Executive Suite"));
        assertEquals(RoomCategory.STUDIO, RoomCategory.fromString("Studio"));
        assertEquals(RoomCategory.FAMILY_SUITE, RoomCategory.fromString("FAMILY_SUITE"));
        assertEquals(RoomCategory.PRESIDENTIAL, RoomCategory.fromString("Presidential Suite"));
        assertEquals(RoomCategory.STANDARD, RoomCategory.fromString("Unknown Type"));
        assertEquals(RoomCategory.STANDARD, RoomCategory.fromString(null));
        assertEquals(RoomCategory.STANDARD, RoomCategory.fromString(""));
    }

    @Test
    @DisplayName("Should create RoomType with explicit category")
    void shouldCreateRoomTypeWithCategory() {
        RoomType roomType = RoomType.builder()
                .id("RT-1")
                .propertyId("PROP-1")
                .category(RoomCategory.DELUXE)
                .name("Deluxe Ocean View")
                .basePricePerNight(Money.of(200.0, "USD"))
                .maxCapacity(3)
                .totalInventory(10)
                .amenities(Set.of())
                .build();

        assertEquals(RoomCategory.DELUXE, roomType.getCategory());
        assertEquals("Deluxe Ocean View", roomType.getName());
        assertEquals("RT-1", roomType.getId());
        assertEquals(3, roomType.getMaxCapacity());
    }

    @Test
    @DisplayName("Should reject RoomType creation when category is null")
    void shouldRejectNullCategory() {
        assertThrows(IllegalArgumentException.class, () ->
                new RoomType("RT-1", "PROP-1", null, "Deluxe",
                        Money.of(200.0, "USD"), 3, 10, Set.of()));
    }
}
