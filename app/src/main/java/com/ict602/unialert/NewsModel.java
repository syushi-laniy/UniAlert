package com.ict602.unialert;

import com.google.firebase.Timestamp;

public class NewsModel {

    public String title;
    public String content;
    public Timestamp createdAt;

    public NewsModel(String title, String content, Timestamp createdAt) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }
}
