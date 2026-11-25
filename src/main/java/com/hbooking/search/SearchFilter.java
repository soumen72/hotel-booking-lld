package com.hbooking.search;

import com.hbooking.model.Money;
import com.hbooking.model.Property;
import com.hbooking.model.RoomType;
import com.hbooking.model.SearchCriteria;

public interface SearchFilter {
    boolean matches(Property property, RoomType roomType, Money calculatedPrice, SearchCriteria criteria);
}
