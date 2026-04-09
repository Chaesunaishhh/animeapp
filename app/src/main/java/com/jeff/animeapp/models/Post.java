package com.jeff.animeapp.models;

public class Post {
    private String userName;
    private String content;
    private String imageUrl;

    public Post(String userName, String content, String imageUrl) {
        this.userName = userName;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public String getUserName() {
        return userName;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
