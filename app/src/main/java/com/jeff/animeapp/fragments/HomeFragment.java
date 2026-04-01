package com.jeff.animeapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jeff.animeapp.R;
import com.jeff.animeapp.adapters.AnimeAdapter;
import com.jeff.animeapp.api.AniListClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private EditText searchInput;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = v.findViewById(R.id.recyclerHome);
        progressBar = v.findViewById(R.id.progressHome);
        searchInput = v.findViewById(R.id.searchInput);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        fetchAnimeList();

        // SEARCH
        searchInput.setOnEditorActionListener((textView, i, keyEvent) -> {
            String searchText = searchInput.getText().toString();
            if (!searchText.isEmpty()) {
                searchAnime(searchText);
            }
            return true;
        });

        // BUTTONS
        Button btnPopular = v.findViewById(R.id.btnPopular);
        Button btnTop = v.findViewById(R.id.btnTop);
        Button btnTrending = v.findViewById(R.id.btnTrending);

        btnPopular.setOnClickListener(view -> fetchBySort("POPULARITY_DESC"));
        btnTop.setOnClickListener(view -> fetchBySort("SCORE_DESC"));
        btnTrending.setOnClickListener(view -> fetchBySort("TRENDING_DESC"));

        return v;
    }

    private void fetchAnimeList() {
        progressBar.setVisibility(View.VISIBLE);

        String query = "query { Page(page: 1, perPage: 20) { media(type: ANIME) { id title { romaji } coverImage { large } averageScore } } }";

        JsonObject body = new JsonObject();
        body.addProperty("query", query);

        AniListClient.API api = AniListClient.getClient().create(AniListClient.API.class);
        api.query(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE);

                try {
                    JsonArray mediaArray = response.body()
                            .getAsJsonObject("data")
                            .getAsJsonObject("Page")
                            .getAsJsonArray("media");

                    recyclerView.setAdapter(new AnimeAdapter(mediaArray));
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error loading data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "API Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchAnime(String search) {
        progressBar.setVisibility(View.VISIBLE);

        String query = "query ($search: String) { Page(page: 1, perPage: 20) { media(search: $search, type: ANIME) { id title { romaji } coverImage { large } averageScore } } }";

        JsonObject variables = new JsonObject();
        variables.addProperty("search", search);

        JsonObject body = new JsonObject();
        body.addProperty("query", query);
        body.add("variables", variables);

        AniListClient.API api = AniListClient.getClient().create(AniListClient.API.class);
        api.query(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE);

                JsonArray mediaArray = response.body()
                        .getAsJsonObject("data")
                        .getAsJsonObject("Page")
                        .getAsJsonArray("media");

                recyclerView.setAdapter(new AnimeAdapter(mediaArray));
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void fetchBySort(String sort) {
        progressBar.setVisibility(View.VISIBLE);

        String query = "query ($sort: [MediaSort]) { Page(page: 1, perPage: 20) { media(type: ANIME, sort: $sort) { id title { romaji } coverImage { large } averageScore } } }";

        JsonObject variables = new JsonObject();
        variables.addProperty("sort", sort);

        JsonObject body = new JsonObject();
        body.addProperty("query", query);
        body.add("variables", variables);

        AniListClient.API api = AniListClient.getClient().create(AniListClient.API.class);
        api.query(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE);

                JsonArray mediaArray = response.body()
                        .getAsJsonObject("data")
                        .getAsJsonObject("Page")
                        .getAsJsonArray("media");

                recyclerView.setAdapter(new AnimeAdapter(mediaArray));
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}