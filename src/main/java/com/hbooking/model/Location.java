package com.hbooking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Locale;
import java.util.Objects;

@Getter
@Builder
@AllArgsConstructor
public final class Location {
    private final String city;
    private final String locality;
    private final String address;

    public boolean matchesCity(String targetCity) {
        if (targetCity == null || targetCity.isBlank()) {
            return false;
        }
        return this.city.equalsIgnoreCase(targetCity.trim());
    }

    public boolean matchesLocality(String targetLocality) {
        if (targetLocality == null || targetLocality.isBlank()) {
            return true;
        }
        return this.locality.equalsIgnoreCase(targetLocality.trim());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location location)) return false;
        return city.equalsIgnoreCase(location.city)
                && locality.equalsIgnoreCase(location.locality)
                && address.equalsIgnoreCase(location.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city.toLowerCase(Locale.ROOT), locality.toLowerCase(Locale.ROOT), address.toLowerCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return address.isBlank() ? locality + ", " + city : address + ", " + locality + ", " + city;
    }
}
