package com.hbooking.policy;

import com.hbooking.model.DateRange;
import com.hbooking.model.Money;
import com.hbooking.model.RoomType;

public interface PricingStrategy {
    Money calculatePrice(RoomType roomType, DateRange dateRange);
}
