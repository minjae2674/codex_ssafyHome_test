package com.ssafy.home.apartment.service;

import com.ssafy.home.apartment.dto.ApartmentSearchResponse;
import com.ssafy.home.apartment.dto.ApartmentSummaryResponse;
import com.ssafy.home.apartment.mapper.ApartmentMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApartmentService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ApartmentMapper apartmentMapper;

    public ApartmentService(ApartmentMapper apartmentMapper) {
        this.apartmentMapper = apartmentMapper;
    }

    public ApartmentSearchResponse searchApartments(
            String keyword,
            String sidoName,
            String gugunName,
            String dongCode,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int offset = (safePage - 1) * safeSize;

        List<ApartmentSummaryResponse> apartments = apartmentMapper.searchApartments(
                keyword,
                sidoName,
                gugunName,
                dongCode,
                safeSize,
                offset
        );
        long totalCount = apartmentMapper.countApartments(keyword, sidoName, gugunName, dongCode);

        return new ApartmentSearchResponse(apartments, safePage, safeSize, totalCount);
    }
}
