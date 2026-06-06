package com.ssafy.home.apartment.dto;

public class ApartmentPriceTrendResponse {

    private Integer dealYear;
    private Integer dealMonth;
    private String label;
    private Long averageAmount;
    private Long dealCount;

    public Integer getDealYear() {
        return dealYear;
    }

    public void setDealYear(Integer dealYear) {
        this.dealYear = dealYear;
    }

    public Integer getDealMonth() {
        return dealMonth;
    }

    public void setDealMonth(Integer dealMonth) {
        this.dealMonth = dealMonth;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getAverageAmount() {
        return averageAmount;
    }

    public void setAverageAmount(Long averageAmount) {
        this.averageAmount = averageAmount;
    }

    public Long getDealCount() {
        return dealCount;
    }

    public void setDealCount(Long dealCount) {
        this.dealCount = dealCount;
    }
}
