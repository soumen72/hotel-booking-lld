package com.hbooking.repository.impl;

import com.hbooking.model.DateRange;
import com.hbooking.repository.InventoryRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryInventoryRepository implements InventoryRepository {
    private final Map<String, Integer> totalCapacityMap = new ConcurrentHashMap<>();
    private final Map<String, Map<LocalDate, Integer>> reservedInventoryMap = new ConcurrentHashMap<>();
    private final Map<String, Object> locks = new ConcurrentHashMap<>();

    @Override
    public void initializeInventory(String roomTypeId, int totalRooms) {
        if (roomTypeId == null || roomTypeId.isBlank()) {
            throw new IllegalArgumentException("Room type ID cannot be blank");
        }
        if (totalRooms <= 0) {
            throw new IllegalArgumentException("Total rooms must be positive");
        }
        totalCapacityMap.put(roomTypeId, totalRooms);
        reservedInventoryMap.putIfAbsent(roomTypeId, new ConcurrentHashMap<>());
    }

    @Override
    public boolean isAvailable(String roomTypeId, DateRange dateRange, int roomsNeeded) {
        Object lock = getLock(roomTypeId);
        synchronized (lock) {
            int total = totalCapacityMap.getOrDefault(roomTypeId, 0);
            if (total < roomsNeeded) {
                return false;
            }
            Map<LocalDate, Integer> reservations = reservedInventoryMap.getOrDefault(roomTypeId, Map.of());
            for (LocalDate date : dateRange.getDates()) {
                int reserved = reservations.getOrDefault(date, 0);
                if (total - reserved < roomsNeeded) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public boolean reserveInventory(String roomTypeId, DateRange dateRange, int roomsNeeded) {
        Object lock = getLock(roomTypeId);
        synchronized (lock) {
            int total = totalCapacityMap.getOrDefault(roomTypeId, 0);
            if (total < roomsNeeded) {
                return false;
            }

            Map<LocalDate, Integer> reservations = reservedInventoryMap.computeIfAbsent(
                    roomTypeId, k -> new ConcurrentHashMap<>());

            // Phase 1: verify capacity on all dates
            for (LocalDate date : dateRange.getDates()) {
                int reserved = reservations.getOrDefault(date, 0);
                if (total - reserved < roomsNeeded) {
                    return false;
                }
            }

            // Phase 2: atomically apply reservation to all dates
            for (LocalDate date : dateRange.getDates()) {
                int reserved = reservations.getOrDefault(date, 0);
                reservations.put(date, reserved + roomsNeeded);
            }
            return true;
        }
    }

    @Override
    public void releaseInventory(String roomTypeId, DateRange dateRange, int roomsNeeded) {
        Object lock = getLock(roomTypeId);
        synchronized (lock) {

            Map<LocalDate, Integer> reservations = reservedInventoryMap.get(roomTypeId);
            if (reservations == null) {
                return;
            }

            for (LocalDate date : dateRange.getDates()) {
                int reserved = reservations.getOrDefault(date, 0);
                int updated = Math.max(0, reserved - roomsNeeded);
                if (updated == 0) {
                    reservations.remove(date);
                } else {
                    reservations.put(date, updated);
                }
            }
        }
    }

    @Override
    public int getAvailableRooms(String roomTypeId, LocalDate date) {
        int total = totalCapacityMap.getOrDefault(roomTypeId, 0);
        Map<LocalDate, Integer> reservations = reservedInventoryMap.getOrDefault(roomTypeId, Map.of());
        int reserved = reservations.getOrDefault(date, 0);
        return Math.max(0, total - reserved);
    }

    private Object getLock(String roomTypeId) {
        return locks.computeIfAbsent(roomTypeId, k -> new Object());
    }
}
