package com.ssafy.home.apartment.dto;

import java.util.List;

public class ApartmentDetailResponse {

    private final ApartmentSummaryResponse apartment;
    private final ApartmentDealStatsResponse dealStats;
    private final ApartmentDealResponse latestDeal;
    private final List<ApartmentPriceTrendResponse> priceTrend;
    private final List<ApartmentDealResponse> recentDeals;

    public ApartmentDetailResponse(
            ApartmentSummaryResponse apartment,
            ApartmentDealStatsResponse dealStats,
            ApartmentDealResponse latestDeal,
            List<ApartmentPriceTrendResponse> priceTrend,
            List<ApartmentDealResponse> recentDeals
    ) {
        this.apartment = apartment;
        this.dealStats = dealStats;
        this.latestDeal = latestDeal;
        this.priceTrend = priceTrend;
        this.recentDeals = recentDeals;
    }

    public ApartmentSummaryResponse getApartment() {
        return apartment;
    }

    public ApartmentDealStatsResponse getDealStats() {
        return dealStats;
    }

    public ApartmentDealResponse getLatestDeal() {
        return latestDeal;
    }

    public List<ApartmentPriceTrendResponse> getPriceTrend() {
        return priceTrend;
    }

    public List<ApartmentDealResponse> getRecentDeals() {
        return recentDeals;
    }
}
