package com.jeff.animeapp.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AnimeCharacterApiService {

    // Get all characters
    @GET("api/v1/characters")
    Call<CharacterResponse> getAllCharacters(@Query("page") int page, @Query("limit") int limit);

    // Search characters by anime
    @GET("api/v1/characters/search")
    Call<CharacterResponse> searchByAnime(@Query("anime") String anime, @Query("limit") int limit);

    // Get random characters
    @GET("api/v1/characters/random")
    Call<CharacterResponse> getRandomCharacters(@Query("limit") int limit);

    // Get character by ID
    @GET("api/v1/characters/{id}")
    Call<CharacterDetailResponse> getCharacterById(@Path("id") int id);

    class CharacterResponse {
        @SerializedName("success")
        private boolean success;

        @SerializedName("message")
        private String message;

        @SerializedName("data")
        private List<CharacterData> data;

        @SerializedName("pagination")
        private Pagination pagination;

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public List<CharacterData> getData() { return data; }
        public Pagination getPagination() { return pagination; }
    }

    class CharacterDetailResponse {
        @SerializedName("success")
        private boolean success;

        @SerializedName("data")
        private CharacterData data;

        public boolean isSuccess() { return success; }
        public CharacterData getData() { return data; }
    }

    class CharacterData {
        @SerializedName("id")
        private int id;

        @SerializedName("firstName")
        private String firstName;

        @SerializedName("lastName")
        private String lastName;

        @SerializedName("fullName")
        private String fullName;

        @SerializedName("anime")
        private String anime;

        @SerializedName("village")
        private String village;

        @SerializedName("element")
        private String element;

        @SerializedName("age")
        private int age;

        @SerializedName("status")
        private String status;

        @SerializedName("image")
        private String imageUrl;

        public String getFullName() {
            return fullName != null ? fullName : (firstName != null ? firstName : "Unknown");
        }

        public String getAnime() { return anime != null ? anime : "Unknown Anime"; }
        public String getImageUrl() { return imageUrl; }
        public int getId() { return id; }
    }

    class Pagination {
        @SerializedName("currentPage")
        private int currentPage;

        @SerializedName("totalPages")
        private int totalPages;

        @SerializedName("totalItems")
        private int totalItems;

        public int getCurrentPage() { return currentPage; }
        public int getTotalPages() { return totalPages; }
        public int getTotalItems() { return totalItems; }
    }
}