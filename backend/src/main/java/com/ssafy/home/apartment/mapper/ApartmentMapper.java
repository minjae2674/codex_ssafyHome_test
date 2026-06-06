package com.ssafy.home.apartment.mapper;

import com.ssafy.home.apartment.dto.ApartmentDealResponse;
import com.ssafy.home.apartment.dto.ApartmentDealStatsResponse;
import com.ssafy.home.apartment.dto.ApartmentPriceTrendResponse;
import com.ssafy.home.apartment.dto.ApartmentSummaryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ApartmentMapper {

    @Select("""
            SELECT
                h.apt_seq AS apt_seq,
                h.apt_nm AS apt_name,
                h.build_year AS build_year,
                d.sido_name AS sido_name,
                d.gugun_name AS gugun_name,
                d.dong_name AS dong_name,
                h.jibun AS jibun,
                h.road_nm AS road_name,
                CAST(NULLIF(h.latitude, '') AS DECIMAL(10, 7)) AS latitude,
                CAST(NULLIF(h.longitude, '') AS DECIMAL(10, 7)) AS longitude
            FROM houseinfos h
            LEFT JOIN dongcodes d
                ON d.dong_code = CONCAT(h.sgg_cd, h.umd_cd)
            WHERE h.apt_seq = #{aptSeq}
            """)
    ApartmentSummaryResponse findApartmentByAptSeq(@Param("aptSeq") String aptSeq);

    @Select("""
            <script>
            SELECT
                h.apt_seq AS apt_seq,
                h.apt_nm AS apt_name,
                h.build_year AS build_year,
                d.sido_name AS sido_name,
                d.gugun_name AS gugun_name,
                d.dong_name AS dong_name,
                h.jibun AS jibun,
                h.road_nm AS road_name,
                CAST(NULLIF(h.latitude, '') AS DECIMAL(10, 7)) AS latitude,
                CAST(NULLIF(h.longitude, '') AS DECIMAL(10, 7)) AS longitude
            FROM houseinfos h
            LEFT JOIN dongcodes d
                ON d.dong_code = CONCAT(h.sgg_cd, h.umd_cd)
            WHERE h.latitude IS NOT NULL
              AND h.longitude IS NOT NULL
            <if test="keyword != null and keyword != ''">
              AND (
                  h.apt_nm LIKE CONCAT('%', #{keyword}, '%')
                  OR h.umd_nm LIKE CONCAT('%', #{keyword}, '%')
                  OR d.gugun_name LIKE CONCAT('%', #{keyword}, '%')
                  OR d.dong_name LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            <if test="sidoName != null and sidoName != ''">
              AND d.sido_name = #{sidoName}
            </if>
            <if test="gugunName != null and gugunName != ''">
              AND d.gugun_name = #{gugunName}
            </if>
            <if test="dongCode != null and dongCode != ''">
              AND d.dong_code = #{dongCode}
            </if>
            ORDER BY h.apt_nm, h.apt_seq
            LIMIT #{size}
            OFFSET #{offset}
            </script>
            """)
    List<ApartmentSummaryResponse> searchApartments(
            @Param("keyword") String keyword,
            @Param("sidoName") String sidoName,
            @Param("gugunName") String gugunName,
            @Param("dongCode") String dongCode,
            @Param("size") int size,
            @Param("offset") int offset
    );

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM houseinfos h
            LEFT JOIN dongcodes d
                ON d.dong_code = CONCAT(h.sgg_cd, h.umd_cd)
            WHERE h.latitude IS NOT NULL
              AND h.longitude IS NOT NULL
            <if test="keyword != null and keyword != ''">
              AND (
                  h.apt_nm LIKE CONCAT('%', #{keyword}, '%')
                  OR h.umd_nm LIKE CONCAT('%', #{keyword}, '%')
                  OR d.gugun_name LIKE CONCAT('%', #{keyword}, '%')
                  OR d.dong_name LIKE CONCAT('%', #{keyword}, '%')
              )
            </if>
            <if test="sidoName != null and sidoName != ''">
              AND d.sido_name = #{sidoName}
            </if>
            <if test="gugunName != null and gugunName != ''">
              AND d.gugun_name = #{gugunName}
            </if>
            <if test="dongCode != null and dongCode != ''">
              AND d.dong_code = #{dongCode}
            </if>
            </script>
            """)
    long countApartments(
            @Param("keyword") String keyword,
            @Param("sidoName") String sidoName,
            @Param("gugunName") String gugunName,
            @Param("dongCode") String dongCode
    );

    @Select("""
            SELECT
                COUNT(*) AS deal_count,
                ROUND(AVG(v.amount)) AS average_amount,
                ROUND(AVG(
                    CASE
                        WHEN v.deal_date >= DATE_SUB(l.latest_date, INTERVAL 3 MONTH)
                        THEN v.amount
                    END
                )) AS recent_average_amount,
                MAX(v.amount) AS highest_amount,
                MIN(v.amount) AS lowest_amount,
                MIN(v.exclu_use_ar) AS min_exclusive_use_area,
                MAX(v.exclu_use_ar) AS max_exclusive_use_area
            FROM (
                SELECT
                    CAST(TRIM(REPLACE(deal_amount, ',', '')) AS UNSIGNED) AS amount,
                    exclu_use_ar,
                    STR_TO_DATE(
                        CONCAT(deal_year, '-', LPAD(deal_month, 2, '0'), '-', LPAD(deal_day, 2, '0')),
                        '%Y-%m-%d'
                    ) AS deal_date
                FROM housedeals
                WHERE apt_seq = #{aptSeq}
                  AND deal_amount IS NOT NULL
                  AND TRIM(deal_amount) != ''
            ) v
            JOIN (
                SELECT MAX(
                    STR_TO_DATE(
                        CONCAT(deal_year, '-', LPAD(deal_month, 2, '0'), '-', LPAD(deal_day, 2, '0')),
                        '%Y-%m-%d'
                    )
                ) AS latest_date
                FROM housedeals
                WHERE apt_seq = #{aptSeq}
            ) l
            WHERE v.deal_date >= DATE_SUB(l.latest_date, INTERVAL 3 YEAR)
            """)
    ApartmentDealStatsResponse findDealStatsByAptSeq(@Param("aptSeq") String aptSeq);

    @Select("""
            SELECT
                no,
                deal_year,
                deal_month,
                deal_day,
                exclu_use_ar AS exclusive_use_area,
                floor,
                CAST(TRIM(REPLACE(deal_amount, ',', '')) AS UNSIGNED) AS deal_amount
            FROM housedeals
            WHERE apt_seq = #{aptSeq}
              AND deal_amount IS NOT NULL
              AND TRIM(deal_amount) != ''
            ORDER BY deal_year DESC, deal_month DESC, deal_day DESC, no DESC
            LIMIT 1
            """)
    ApartmentDealResponse findLatestDealByAptSeq(@Param("aptSeq") String aptSeq);

    @Select("""
            SELECT
                v.deal_year,
                v.deal_month,
                CONCAT(v.deal_year, '.', LPAD(v.deal_month, 2, '0')) AS label,
                ROUND(AVG(v.amount)) AS average_amount,
                COUNT(*) AS deal_count
            FROM (
                SELECT
                    deal_year,
                    deal_month,
                    CAST(TRIM(REPLACE(deal_amount, ',', '')) AS UNSIGNED) AS amount,
                    STR_TO_DATE(
                        CONCAT(deal_year, '-', LPAD(deal_month, 2, '0'), '-', LPAD(deal_day, 2, '0')),
                        '%Y-%m-%d'
                    ) AS deal_date
                FROM housedeals
                WHERE apt_seq = #{aptSeq}
                  AND deal_amount IS NOT NULL
                  AND TRIM(deal_amount) != ''
            ) v
            JOIN (
                SELECT MAX(
                    STR_TO_DATE(
                        CONCAT(deal_year, '-', LPAD(deal_month, 2, '0'), '-', LPAD(deal_day, 2, '0')),
                        '%Y-%m-%d'
                    )
                ) AS latest_date
                FROM housedeals
                WHERE apt_seq = #{aptSeq}
            ) l
            WHERE v.deal_date >= DATE_SUB(l.latest_date, INTERVAL 12 MONTH)
            GROUP BY v.deal_year, v.deal_month
            ORDER BY v.deal_year, v.deal_month
            """)
    List<ApartmentPriceTrendResponse> findPriceTrendByAptSeq(@Param("aptSeq") String aptSeq);

    @Select("""
            SELECT
                no,
                deal_year,
                deal_month,
                deal_day,
                exclu_use_ar AS exclusive_use_area,
                floor,
                CAST(TRIM(REPLACE(deal_amount, ',', '')) AS UNSIGNED) AS deal_amount
            FROM housedeals
            WHERE apt_seq = #{aptSeq}
              AND deal_amount IS NOT NULL
              AND TRIM(deal_amount) != ''
            ORDER BY deal_year DESC, deal_month DESC, deal_day DESC, no DESC
            LIMIT #{limit}
            """)
    List<ApartmentDealResponse> findRecentDealsByAptSeq(
            @Param("aptSeq") String aptSeq,
            @Param("limit") int limit
    );
}
