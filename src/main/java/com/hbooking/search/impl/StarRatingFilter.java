package com.hbooking.search.impl;

import com.hbooking.model.Money;
import com.hbooking.model.Property;
import com.hbooking.model.RoomType;
import com.hbooking.model.SearchCriteria;
import com.hbooking.search.SearchFilter;
import org.springframework.stereotype.Component;

@Component
public class StarRatingFilter implements SearchFilter {

    @Override
    public boolean matches(Property property, RoomType roomType, Money calculatedPrice, SearchCriteria criteria) {
        if (criteria == null || criteria.getMinStarRating() == null) {
            return true;
        }

        return property.getStarRating().getValue() >= criteria.getMinStarRating().getValue();
    }
}
