package com.example.doit;

public class Friends {
    String profileImageUrl;  // 이미지 경로
    String status;  // 상태(친구, 친구x)
    String username; // 닉네임

    public Friends() {}

    public Friends(String profileImageUrl, String status, String username) {
        this.profileImageUrl = profileImageUrl;
        this.status = status;
        this.username = username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}


