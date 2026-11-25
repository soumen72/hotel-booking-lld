package com.hbooking.repository.impl;

import com.hbooking.model.Property;
import com.hbooking.repository.PropertyRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryPropertyRepository implements PropertyRepository {
    private final Map<String, Property> storage = new ConcurrentHashMap<>();

    @Override
    public Property save(Property property) {
        if (property == null) {
            throw new IllegalArgumentException("Property cannot be null");
        }
        storage.put(property.getId(), property);
        return property;
    }

    @Override
    public Optional<Property> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Property> findByOwnerId(String ownerId) {
        if (ownerId == null) {
            return List.of();
        }
        return storage.values().stream()
                .filter(p -> {
                    String pOwnerId = p.getOwnerId();
                    return pOwnerId.equalsIgnoreCase(ownerId);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Property> findByLocation(String city, String locality) {
        return storage.values().stream()
                .filter(p -> p.getLocation().matchesCity(city))
                .filter(p -> p.getLocation().matchesLocality(locality))
                .collect(Collectors.toList());
    }

    @Override
    public List<Property> findAll() {
        return new ArrayList<>(storage.values());
    }
}
