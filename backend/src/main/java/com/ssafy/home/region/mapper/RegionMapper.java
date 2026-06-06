package com.ssafy.home.region.mapper;

import com.ssafy.home.region.dto.RegionResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RegionMapper {

    @Select("""
            SELECT
                LEFT(MIN(d.dong_code), 2) AS code,
                d.sido_name AS name,
                ROUND(AVG(CAST(NULLIF(h.latitude, '') AS DECIMAL(10, 7))), 7) AS center_lat,
                ROUND(AVG(CAST(NULLIF(h.longitude, '') AS DECIMAL(10, 7))), 7) AS center_lng,
                COUNT(h.apt_seq) AS apartment_count
            FROM dongcodes d
            LEFT JOIN houseinfos h
                ON d.dong_code = CONCAT(h.sgg_cd, h.umd_cd)
            WHERE d.sido_name IS NOT NULL
            GROUP BY d.sido_name
            HAVING apartment_count > 0
            ORDER BY MIN(d.dong_code)
            """)
    List<RegionResponse> selectSidos();

    @Select("""
            SELECT
                LEFT(MIN(d.dong_code), 5) AS code,
                d.gugun_name AS name,
                ROUND(AVG(CAST(NULLIF(h.latitude, '') AS DECIMAL(10, 7))), 7) AS center_lat,
                ROUND(AVG(CAST(NULLIF(h.longitude, '') AS DECIMAL(10, 7))), 7) AS center_lng,
                COUNT(h.apt_seq) AS apartment_count
            FROM dongcodes d
            LEFT JOIN houseinfos h
                ON d.dong_code = CONCAT(h.sgg_cd, h.umd_cd)
            WHERE d.sido_name = #{sidoName}
              AND d.gugun_name IS NOT NULL
            GROUP BY d.gugun_name
            HAVING apartment_count > 0
            ORDER BY MIN(d.dong_code)
            """)
    List<RegionResponse> selectGuguns(@Param("sidoName") String sidoName);

    @Select("""
            SELECT
                d.dong_code AS code,
                d.dong_name AS name,
                ROUND(AVG(CAST(NULLIF(h.latitude, '') AS DECIMAL(10, 7))), 7) AS center_lat,
                ROUND(AVG(CAST(NULLIF(h.longitude, '') AS DECIMAL(10, 7))), 7) AS center_lng,
                COUNT(h.apt_seq) AS apartment_count
            FROM dongcodes d
            LEFT JOIN houseinfos h
                ON d.dong_code = CONCAT(h.sgg_cd, h.umd_cd)
            WHERE d.sido_name = #{sidoName}
              AND d.gugun_name = #{gugunName}
              AND d.dong_name IS NOT NULL
            GROUP BY d.dong_code, d.dong_name
            HAVING apartment_count > 0
            ORDER BY d.dong_code
            """)
    List<RegionResponse> selectDongs(
            @Param("sidoName") String sidoName,
            @Param("gugunName") String gugunName
    );
}
