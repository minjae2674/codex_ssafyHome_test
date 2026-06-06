package com.ssafy.home.apartment.controller;

import com.ssafy.home.apartment.dto.ApartmentSearchResponse;
import com.ssafy.home.apartment.service.ApartmentService;
import com.ssafy.home.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/apartments")
public class ApartmentController {

    private final ApartmentService apartmentService;

    public ApartmentController(ApartmentService apartmentService) {
        this.apartmentService = apartmentService;
    }

    @GetMapping("/search")
    public ApiResponse<ApartmentSearchResponse> searchApartments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sidoName,
            @RequestParam(required = false) String gugunName,
            @RequestParam(required = false) String dongCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        ApartmentSearchResponse response = apartmentService.searchApartments(
                keyword,
                sidoName,
                gugunName,
                dongCode,
                page,
                size
        );
        return ApiResponse.ok(response);
    }
}
