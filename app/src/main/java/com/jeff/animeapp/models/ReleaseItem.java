package com.jeff.animeapp.models;

public class ReleaseItem {
    private String title;
    private String releaseDate;
    private String type;     // e.g. "TV" or "Movie"
    private String tags;     // e.g. "Action, Fantasy"
    private String imageUrl; // thumbnail link

    public ReleaseItem(String title, String releaseDate, String type, String tags, String imageUrl) {
        this.title = title;
        this.releaseDate = releaseDate;
        this.type = type;
        this.tags = tags;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getTitle() {
        return title;
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

    // Setters (optional, if you want to modify data later)
    public void setTitle(String title) {
        this.title = title;
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
