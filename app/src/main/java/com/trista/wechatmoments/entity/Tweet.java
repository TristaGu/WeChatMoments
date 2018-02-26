package com.trista.wechatmoments.entity;

import java.util.List;

/**
 * The tweet entity.
 * <p>
 * Created by Trista on 2018/2/25.
 */
public class Tweet {
    private String content;
    private List<Image> images;
    private Account sender;
    private List<Comment> comments;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public Account getSender() {
        return sender;
    }

    public void setSender(Account sender) {
        this.sender = sender;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
