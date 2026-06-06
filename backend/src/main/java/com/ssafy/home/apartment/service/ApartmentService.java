package com.ssafy.home.apartment.service;

import com.ssafy.home.apartment.dto.ApartmentDealResponse;
import com.ssafy.home.apartment.dto.ApartmentDealStatsResponse;
import com.ssafy.home.apartment.dto.ApartmentDetailResponse;
import com.ssafy.home.apartment.dto.ApartmentPriceTrendResponse;
import com.ssafy.home.apartment.dto.ApartmentSearchResponse;
import com.ssafy.home.apartment.dto.ApartmentSummaryResponse;
import com.ssafy.home.apartment.mapper.ApartmentMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public ApartmentDetailResponse getApartmentDetail(String aptSeq) {
        ApartmentSummaryResponse apartment = apartmentMapper.findApartmentByAptSeq(aptSeq);

        if (apartment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "아파트를 찾을 수 없습니다.");
        }

        // 화면의 단지 상세 패널에서 필요한 거래 통계는 모두 housedeals에서 계산한다.
        ApartmentDealStatsResponse dealStats = apartmentMapper.findDealStatsByAptSeq(aptSeq);
        ApartmentDealResponse latestDeal = apartmentMapper.findLatestDealByAptSeq(aptSeq);
        List<ApartmentPriceTrendResponse> priceTrend = apartmentMapper.findPriceTrendByAptSeq(aptSeq);
        List<ApartmentDealResponse> recentDeals = apartmentMapper.findRecentDealsByAptSeq(aptSeq, 10);

        return new ApartmentDetailResponse(
                apartment,
                dealStats,
                latestDeal,
                priceTrend,
                recentDeals
        );
    }

    public List<ApartmentDealResponse> getApartmentDeals(String aptSeq) {
        ApartmentSummaryResponse apartment = apartmentMapper.findApartmentByAptSeq(aptSeq);

        if (apartment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "아파트를 찾을 수 없습니다.");
        }

        return apartmentMapper.findRecentDealsByAptSeq(aptSeq, 30);
    }
}
