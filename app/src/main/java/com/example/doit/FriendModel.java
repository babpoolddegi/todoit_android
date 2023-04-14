package com.example.doit;

public class FriendModel {
    String username;  // 사용자 이름 (닉네임)
    String profileImageUrl; // 사용자 프로필 사진 url
    static String uid; // 현재 로그인한 사용자
    String current;
    String userID;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public FriendModel() {
    }

    public FriendModel(String username, String profileImgUrl, String uid) {
        this.username = username;
        this.profileImageUrl = profileImgUrl;
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String userName) {
        this.username = userName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImgUrl) {
        this.profileImageUrl = profileImgUrl;
    }

    public static String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }
}
