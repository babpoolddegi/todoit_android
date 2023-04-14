package com.example.doit;

public class UserModel {
    String userName;  // 사용자 이름 (닉네임)
    String profileImgUrl; // 사용자 프로필 사진 url
    static String uid; // 현재 로그인한 사용자
    String current;


    public UserModel() {
    }

    public UserModel(String userName, String profileImgUrl, String uid, String current) {
        this.userName = userName;
        this.profileImgUrl = profileImgUrl;
        this.uid = uid;
        this.current = current;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProfileImgUrl() {
        return profileImgUrl;
    }

    public void setProfileImgUrl(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
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
