package com.trista.wechatmoments.entity;

import com.google.gson.annotations.SerializedName;

/**
 * The account entity.
 * <p>
 * Created by Trista on 2018/2/25.
 */
public class Account {
    private String username;
    @SerializedName("nick")
    private String nickname;
    private String avatar;
    @SerializedName("profile-image")
    private String profile;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
