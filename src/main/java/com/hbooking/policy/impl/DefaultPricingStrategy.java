package com.hbooking.policy.impl;

import com.hbooking.model.DateRange;
import com.hbooking.model.Money;
import com.hbooking.model.RoomType;
import com.hbooking.policy.PricingStrategy;
import org.springframework.stereotype.Component;

@Component
public class DefaultPricingStrategy implements PricingStrategy {

    @Override
    public Money calculatePrice(RoomType roomType, DateRange dateRange) {
        if (roomType == null || dateRange == null) {
            throw new IllegalArgumentException("Room type and date range cannot be null");
        }
        long nights = dateRange.getNumberOfNights(); //3
        Money basePricePerNight = roomType.getBasePricePerNight(); //1000, curr
        return basePricePerNight.multiply((double) nights);  //3000
    }
}
