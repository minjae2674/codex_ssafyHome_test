package com.ssafy.home.region.dto;

public class RegionResponse {

    private String code;
    private String name;
    private Double centerLat;
    private Double centerLng;
    private Long apartmentCount;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getCenterLat() {
        return centerLat;
    }

    public void setCenterLat(Double centerLat) {
        this.centerLat = centerLat;
    }

    public Double getCenterLng() {
        return centerLng;
    }

    public void setCenterLng(Double centerLng) {
        this.centerLng = centerLng;
    }

    public Long getApartmentCount() {
        return apartmentCount;
    }

    public void setApartmentCount(Long apartmentCount) {
        this.apartmentCount = apartmentCount;
    }
}
