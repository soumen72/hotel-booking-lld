package com.hbooking.repository;

import com.hbooking.model.DateRange;

import java.time.LocalDate;

public interface InventoryRepository {
    void initializeInventory(String roomTypeId, int totalRooms);
    boolean isAvailable(String roomTypeId, DateRange dateRange, int roomsNeeded);
    boolean reserveInventory(String roomTypeId, DateRange dateRange, int roomsNeeded);
    void releaseInventory(String roomTypeId, DateRange dateRange, int roomsNeeded);
    int getAvailableRooms(String roomTypeId, LocalDate date);
}
