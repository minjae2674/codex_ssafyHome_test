package com.ssafy.home.auth.service;

import com.ssafy.home.auth.dto.AuthResponse;
import com.ssafy.home.auth.dto.LoginRequest;
import com.ssafy.home.auth.dto.SignupRequest;
import com.ssafy.home.auth.dto.User;
import com.ssafy.home.auth.dto.UserResponse;
import com.ssafy.home.auth.mapper.UserMapper;
import com.ssafy.home.auth.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse signup(SignupRequest request) {
        validateSignupRequest(request);

        String email = request.getEmail().trim().toLowerCase();
        if (userMapper.countByEmail(email) > 0) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname().trim());
        user.setRole("ROLE_USER");
        user.setProvider("LOCAL");
        user.setProviderId(null);

        // insert 후 MyBatis가 생성된 user_id를 user 객체에 채워준다.
        userMapper.insertUser(user);

        String accessToken = jwtTokenProvider.createToken(user);
        return new AuthResponse(accessToken, UserResponse.from(user));
    }

    public AuthResponse login(LoginRequest request) {
        validateLoginRequest(request);

        String email = request.getEmail().trim().toLowerCase();
        User user = userMapper.findByEmail(email);

        if (
                user == null
                || !"LOCAL".equals(user.getProvider())
                || !passwordEncoder.matches(request.getPassword(), user.getPassword())
        ) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createToken(user);
        return new AuthResponse(accessToken, UserResponse.from(user));
    }

    public UserResponse getCurrentUser(Long userId) {
        User user = userMapper.findByUserId(userId);

        if (user == null) {
            throw new IllegalArgumentException("사용자 정보를 찾을 수 없습니다.");
        }

        return UserResponse.from(user);
    }

    private void validateSignupRequest(SignupRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("회원가입 정보를 입력해주세요.");
        }

        validateEmail(request.getEmail());
        validatePassword(request.getPassword());

        if (!StringUtils.hasText(request.getNickname())) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }
    }

    private void validateLoginRequest(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("로그인 정보를 입력해주세요.");
        }

        validateEmail(request.getEmail());

        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }
    }

    private void validateEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            throw new IllegalArgumentException("올바른 이메일을 입력해주세요.");
        }
    }

    private void validatePassword(String password) {
        if (!StringUtils.hasText(password) || password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상 입력해주세요.");
        }
    }
}
