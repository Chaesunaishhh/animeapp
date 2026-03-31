package com.jeff.animeapp.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class KitsuClient {

    private static final String BASE_URL = "https://kitsu.io/api/edge/";

    private static Retrofit retrofit = null;

    // Singleton Retrofit client
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // API interface for Kitsu
    public interface API {
        @GET("posts")
        Call<JsonObject> getCommunityPosts();
    }
}