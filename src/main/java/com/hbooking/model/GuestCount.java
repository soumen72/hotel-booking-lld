package com.hbooking.model;

import lombok.Getter;

import java.util.Objects;

@Getter
public final class GuestCount {
    private final int adults;
    private final int children;

    public GuestCount(int adults, int children) {
        if (adults < 1) {
            throw new IllegalArgumentException("At least 1 adult is required");
        }
        if (children < 0) {
            throw new IllegalArgumentException("Children count cannot be negative");
        }
        this.adults = adults;
        this.children = children;
    }

    public static GuestCount of(int adults) {
        return new GuestCount(adults, 0);
    }

    public static GuestCount of(int adults, int children) {
        return new GuestCount(adults, children);
    }

    public int getTotal() {
        return adults + children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GuestCount that)) return false;
        return adults == that.adults && children == that.children;
    }

    @Override
    public int hashCode() {
        return Objects.hash(adults, children);
    }

    @Override
    public String toString() {
        return adults + " Adult(s), " + children + " Child(ren)";
    }
}
