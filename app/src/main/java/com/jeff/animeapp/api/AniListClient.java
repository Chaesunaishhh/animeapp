package com.jeff.animeapp.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class AniListClient {

    private static final String BASE_URL = "https://graphql.anilist.co/";

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

    // API interface for GraphQL queries
    public interface API {
        @Headers("Content-Type: application/json")
        @POST(".")
        Call<JsonObject> query(@Body JsonObject body);
    }
}