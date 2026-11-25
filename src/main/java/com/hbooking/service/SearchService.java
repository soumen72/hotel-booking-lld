package com.hbooking.service;

import com.hbooking.model.SearchCriteria;
import com.hbooking.model.SearchResult;

import java.util.List;

public interface SearchService {
    List<SearchResult> search(SearchCriteria criteria);
}
