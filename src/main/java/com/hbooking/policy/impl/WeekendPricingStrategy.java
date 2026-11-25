package com.hbooking.policy.impl;

import com.hbooking.model.DateRange;
import com.hbooking.model.Money;
import com.hbooking.model.RoomType;
import com.hbooking.policy.PricingStrategy;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class WeekendPricingStrategy implements PricingStrategy {
    private final double weekendMultiplier;

    public WeekendPricingStrategy() {
        this(1.20); // 20% surge on weekend nights
    }

    public WeekendPricingStrategy(double weekendMultiplier) {
        if (weekendMultiplier <= 0) {
            throw new IllegalArgumentException("Multiplier must be positive");
        }
        this.weekendMultiplier = weekendMultiplier;
    }

    @Override
    public Money calculatePrice(RoomType roomType, DateRange dateRange) {
        if (roomType == null || dateRange == null) {
            throw new IllegalArgumentException("Room type and date range cannot be null");
        }
        Money base = roomType.getBasePricePerNight();
        Money total = Money.zero(base.getCurrency());

        for (LocalDate date : dateRange.getDates()) {
            DayOfWeek dow = date.getDayOfWeek();
            if (dow == DayOfWeek.FRIDAY || dow == DayOfWeek.SATURDAY) {
                total = total.add(base.multiply(weekendMultiplier));
            } else {
                total = total.add(base);
            }
        }
        return total;
    }
}
