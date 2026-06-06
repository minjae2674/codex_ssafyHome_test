package com.ssafy.home.auth.dto;

public class UserResponse {

    private final Long userId;
    private final String email;
    private final String nickname;
    private final String role;

    public UserResponse(Long userId, String email, String nickname, String role) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
    }

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole()
        );
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public String getRole() {
        return role;
    }
}
