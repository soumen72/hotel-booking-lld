package com.hbooking.enums;

public enum RoomCategory {
    STANDARD(1, 4),
    DELUXE(1, 4),
    SUITE(1, 6),
    STUDIO(1, 4),
    FAMILY_SUITE(2, 8),
    PRESIDENTIAL(1, 12);

    private final int minCapacity;
    private final int maxCapacity;

    RoomCategory(int minCapacity, int maxCapacity) {
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
    }

    public int getMinCapacity() {
        return minCapacity;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public boolean isValidCapacity(int capacity) {
        return capacity >= minCapacity && capacity <= maxCapacity;
    }

    public static RoomCategory fromString(String value) {
        if (value == null || value.isBlank()) {
            return STANDARD;
        }
        String normalized = value.trim().toUpperCase().replace(" ", "_").replace("-", "_");
        for (RoomCategory category : values()) {
            if (category.name().equals(normalized)) {
                return category;
            }
        }
        // Match specific keywords (compound / longer tiers first)
        if (normalized.contains("PRESIDENTIAL")) {
            return PRESIDENTIAL;
        }
        if (normalized.contains("FAMILY")) {
            return FAMILY_SUITE;
        }
        if (normalized.contains("SUITE")) {
            return SUITE;
        }
        if (normalized.contains("DELUXE")) {
            return DELUXE;
        }
        if (normalized.contains("STUDIO")) {
            return STUDIO;
        }
        if (normalized.contains("STANDARD")) {
            return STANDARD;
        }
        return STANDARD;
    }
}
