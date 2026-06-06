package com.ssafy.home.region.controller;

import com.ssafy.home.common.ApiResponse;
import com.ssafy.home.region.dto.RegionResponse;
import com.ssafy.home.region.service.RegionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
public class RegionController {

    private final RegionService regionService;

    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    @GetMapping("/sidos")
    public ApiResponse<List<RegionResponse>> getSidos() {
        return ApiResponse.ok(regionService.getSidos());
    }

    @GetMapping("/guguns")
    public ApiResponse<List<RegionResponse>> getGuguns(@RequestParam String sidoName) {
        return ApiResponse.ok(regionService.getGuguns(sidoName));
    }

    @GetMapping("/dongs")
    public ApiResponse<List<RegionResponse>> getDongs(
            @RequestParam String sidoName,
            @RequestParam String gugunName
    ) {
        return ApiResponse.ok(regionService.getDongs(sidoName, gugunName));
    }
}
