package com.hbooking.repository.impl;

import com.hbooking.model.Owner;
import com.hbooking.repository.OwnerRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryOwnerRepository implements OwnerRepository {
    private final Map<String, Owner> storage = new ConcurrentHashMap<>();

    @Override
    public Owner save(Owner owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner cannot be null");
        }
        storage.put(owner.getId(), owner);
        return owner;
    }

    @Override
    public Optional<Owner> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Owner> findAll() {
        return new ArrayList<>(storage.values());
    }
}
