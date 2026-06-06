package com.ssafy.home.apartment.dto;

import java.util.List;

public class ApartmentSearchResponse {

    private final List<ApartmentSummaryResponse> apartments;
    private final int page;
    private final int size;
    private final long totalCount;

    public ApartmentSearchResponse(
            List<ApartmentSummaryResponse> apartments,
            int page,
            int size,
            long totalCount
    ) {
        this.apartments = apartments;
        this.page = page;
        this.size = size;
        this.totalCount = totalCount;
    }

    public List<ApartmentSummaryResponse> getApartments() {
        return apartments;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalCount() {
        return totalCount;
    }
}
