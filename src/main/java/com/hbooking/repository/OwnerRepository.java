package com.hbooking.repository;

import com.hbooking.model.Owner;

import java.util.List;
import java.util.Optional;

public interface OwnerRepository {
    Owner save(Owner owner);
    Optional<Owner> findById(String id);
    List<Owner> findAll();
}
