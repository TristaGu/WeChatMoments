package com.trista.wechatmoments.entity;

/**
 * The comment entity.
 * <p>
 * Created by Trista on 2018/2/25.
 */
public class Comment {
    private String content;
    private Account sender;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Account getSender() {
        return sender;
    }

    public void setSender(Account sender) {
        this.sender = sender;
    }
}
