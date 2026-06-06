package com.ssafy.home.auth.mapper;

import com.ssafy.home.auth.dto.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("""
            SELECT
                user_id,
                email,
                password,
                nickname,
                role,
                provider,
                provider_id,
                created_at,
                updated_at
            FROM users
            WHERE email = #{email}
            """)
    User findByEmail(@Param("email") String email);

    @Select("""
            SELECT
                user_id,
                email,
                password,
                nickname,
                role,
                provider,
                provider_id,
                created_at,
                updated_at
            FROM users
            WHERE user_id = #{userId}
            """)
    User findByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(*)
            FROM users
            WHERE email = #{email}
            """)
    int countByEmail(@Param("email") String email);

    @Insert("""
            INSERT INTO users (
                email,
                password,
                nickname,
                role,
                provider,
                provider_id
            ) VALUES (
                #{email},
                #{password},
                #{nickname},
                #{role},
                #{provider},
                #{providerId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "userId", keyColumn = "user_id")
    void insertUser(User user);
}
