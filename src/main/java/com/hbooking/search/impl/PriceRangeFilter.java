package com.hbooking.search.impl;

import com.hbooking.model.Money;
import com.hbooking.model.Property;
import com.hbooking.model.RoomType;
import com.hbooking.model.SearchCriteria;
import com.hbooking.search.SearchFilter;
import org.springframework.stereotype.Component;

@Component
public class PriceRangeFilter implements SearchFilter {

    @Override
    public boolean matches(Property property, RoomType roomType, Money calculatedPrice, SearchCriteria criteria) {
        if (criteria == null || calculatedPrice == null) {
            return true;
        }

        if (criteria.getMinPrice() != null && calculatedPrice.isLessThan(criteria.getMinPrice())) {
            return false;
        }

        if (criteria.getMaxPrice() != null && calculatedPrice.isGreaterThan(criteria.getMaxPrice())) {
            return false;
        }

        return true;
    }
}
