package com.ssafy.home.apartment.dto;

import java.math.BigDecimal;

public class ApartmentDealResponse {

    private Integer no;
    private Integer dealYear;
    private Integer dealMonth;
    private Integer dealDay;
    private BigDecimal exclusiveUseArea;
    private String floor;
    private Long dealAmount;

    public Integer getNo() {
        return no;
    }

    public void setNo(Integer no) {
        this.no = no;
    }

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

    public Integer getDealDay() {
        return dealDay;
    }

    public void setDealDay(Integer dealDay) {
        this.dealDay = dealDay;
    }

    public BigDecimal getExclusiveUseArea() {
        return exclusiveUseArea;
    }

    public void setExclusiveUseArea(BigDecimal exclusiveUseArea) {
        this.exclusiveUseArea = exclusiveUseArea;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public Long getDealAmount() {
        return dealAmount;
    }

    public void setDealAmount(Long dealAmount) {
        this.dealAmount = dealAmount;
    }
}
