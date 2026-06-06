package com.ssafy.home.apartment.mapper;

import com.ssafy.home.apartment.dto.ApartmentSummaryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ApartmentMapper {

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
}
