package com.ssafy.home.apartment.dto;

import java.math.BigDecimal;

public class ApartmentDealStatsResponse {

    private Long dealCount;
    private Long averageAmount;
    private Long recentAverageAmount;
    private Long highestAmount;
    private Long lowestAmount;
    private BigDecimal minExclusiveUseArea;
    private BigDecimal maxExclusiveUseArea;

    public Long getDealCount() {
        return dealCount;
    }

    public void setDealCount(Long dealCount) {
        this.dealCount = dealCount;
    }

    public Long getAverageAmount() {
        return averageAmount;
    }

    public void setAverageAmount(Long averageAmount) {
        this.averageAmount = averageAmount;
    }

    public Long getRecentAverageAmount() {
        return recentAverageAmount;
    }

    public void setRecentAverageAmount(Long recentAverageAmount) {
        this.recentAverageAmount = recentAverageAmount;
    }

    public Long getHighestAmount() {
        return highestAmount;
    }

    public void setHighestAmount(Long highestAmount) {
        this.highestAmount = highestAmount;
    }

    public Long getLowestAmount() {
        return lowestAmount;
    }

    public void setLowestAmount(Long lowestAmount) {
        this.lowestAmount = lowestAmount;
    }

    public BigDecimal getMinExclusiveUseArea() {
        return minExclusiveUseArea;
    }

    public void setMinExclusiveUseArea(BigDecimal minExclusiveUseArea) {
        this.minExclusiveUseArea = minExclusiveUseArea;
    }

    public BigDecimal getMaxExclusiveUseArea() {
        return maxExclusiveUseArea;
    }

    public void setMaxExclusiveUseArea(BigDecimal maxExclusiveUseArea) {
        this.maxExclusiveUseArea = maxExclusiveUseArea;
    }
}
