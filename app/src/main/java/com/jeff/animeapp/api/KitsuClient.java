package com.jeff.animeapp.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class KitsuClient {

    private static final String BASE_URL = "https://kitsu.io/api/edge/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public interface API {

        @GET("anime")
        Call<JsonObject> searchAnime(@Query("filter[text]") String title);

        // Get trending anime
        @GET("trending/anime")
        Call<JsonObject> getTrendingAnime();

        // Get reviews for a specific anime by ID
        @GET("anime/{id}/reviews")
        Call<JsonObject> getAnimeReviews(@Path("id") int animeId);

        // Add a new review
        @POST("reviews")
        Call<JsonObject> addReview(@Body JsonObject body);
    }
}
