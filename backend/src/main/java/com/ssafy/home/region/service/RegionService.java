package com.ssafy.home.region.service;

import com.ssafy.home.region.dto.RegionResponse;
import com.ssafy.home.region.mapper.RegionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegionService {

    private final RegionMapper regionMapper;

    public RegionService(RegionMapper regionMapper) {
        this.regionMapper = regionMapper;
    }

    public List<RegionResponse> getSidos() {
        return regionMapper.selectSidos();
    }

    public List<RegionResponse> getGuguns(String sidoName) {
        return regionMapper.selectGuguns(sidoName);
    }

    public List<RegionResponse> getDongs(String sidoName, String gugunName) {
        return regionMapper.selectDongs(sidoName, gugunName);
    }
}
