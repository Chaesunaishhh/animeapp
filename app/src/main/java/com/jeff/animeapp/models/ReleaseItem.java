package com.jeff.animeapp.models;

public class ReleaseItem {
    private String title;
    private String date;
    private String releaseDate;
    private String type;
    private String tags;
    private String imageUrl;

    public ReleaseItem(String title, String date, String releaseDate, String type, String tags, String imageUrl) {
        this.title = title;
        this.date = date;
        this.releaseDate = releaseDate;
        this.type = type;
        this.tags = tags;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }
    public String getDate() {
        return date;
    }
    public String getReleaseDate() {
        return releaseDate;
    }
    public String getType() {
        return type;
    }
    public String getTags() {
        return tags;
    }
    public String getImageUrl() {
        return imageUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setTags(String tags) {
        this.tags = tags;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
