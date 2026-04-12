package com.jeff.animeapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jeff.animeapp.MainActivity;
import com.jeff.animeapp.R;
import com.jeff.animeapp.adapters.AnimeAdapter;
import com.jeff.animeapp.api.AniListClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerFeatured, recyclerTrending, recyclerHome;
    private ProgressBar progressBar;
    private EditText searchInput;
    private ImageView filterIcon, searchIcon;
    private RecyclerView recyclerSearchResults;
    private TextView tvSearchResults;
    private AnimeAdapter adapterFeatured, adapterTrending, adapterHome;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Views
        v.findViewById(R.id.btnLeaderboard).setOnClickListener(view ->
                navigateTo(new LeaderboardFragment()));
        recyclerFeatured = v.findViewById(R.id.recyclerFeatured);
        recyclerTrending = v.findViewById(R.id.recyclerTrending);
        recyclerHome = v.findViewById(R.id.recyclerHome);
        progressBar = v.findViewById(R.id.progressHome);
        searchInput = v.findViewById(R.id.searchInput);
        filterIcon = v.findViewById(R.id.ic_filter);
        searchIcon = v.findViewById(R.id.ic_search);
        recyclerSearchResults = v.findViewById(R.id.recyclerSearchResults);
        tvSearchResults = v.findViewById(R.id.tvSearchResults);

        // Layout managers
        recyclerFeatured.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerTrending.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerHome.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerSearchResults.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // API Calls
        fetchFeatured();
        fetchTrending();
        fetchRecommended();

        // Search Logic
        searchIcon.setOnClickListener(view -> {
            String searchText = searchInput.getText().toString().trim();
            if (!searchText.isEmpty()) searchAnime(searchText);
            else Toast.makeText(getContext(), "Enter an anime title", Toast.LENGTH_SHORT).show();
        });

        // Filter Icon
        filterIcon.setOnClickListener(view -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showFilterDialog(this);
            }
        });

        // ✅ QUICK ACCESS: CALENDAR
        v.findViewById(R.id.btnCalendar).setOnClickListener(view ->
                navigateTo(new ReleaseCalendarFragment()));

        // ✅ QUICK ACCESS: QUIZ
        v.findViewById(R.id.btnQuiz).setOnClickListener(view ->
                navigateTo(new QuizFragment()));

        return v;
    }

    // ✅ ADDED THIS METHOD TO FIX MAINACTIVITY ERROR
    public void applyFilters(List<String> genres, List<String> years) {
        if (adapterHome == null || adapterHome.getMediaArray() == null) return;

        JsonArray filteredArray = new JsonArray();
        JsonArray originalData = adapterHome.getMediaArray();

        for (int i = 0; i < originalData.size(); i++) {
            JsonObject anime = originalData.get(i).getAsJsonObject();

            // Extract Genres
            String genreString = anime.has("genres") ? anime.getAsJsonArray("genres").toString() : "";
            // Extract Year
            String year = anime.has("seasonYear") && !anime.get("seasonYear").isJsonNull()
                    ? anime.get("seasonYear").getAsString() : "";

            boolean matchGenre = genres.isEmpty() || genres.stream().anyMatch(genreString::contains);
            boolean matchYear = years.isEmpty() || years.contains(year);

            if (matchGenre && matchYear) {
                filteredArray.add(anime);
            }
        }

        adapterHome.updateData(filteredArray);

        if (filteredArray.size() == 0) {
            Toast.makeText(getContext(), "No anime matches these filters", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateTo(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void fetchFeatured() {
        String query = "query { Page(page: 1, perPage: 10) { media(type: ANIME, sort: POPULARITY_DESC) { id title { romaji } coverImage { large } averageScore description } } }";
        executeApiCall(query, null, "featured");
    }

    private void fetchTrending() {
        String query = "query { Page(page: 1, perPage: 10) { media(type: ANIME, sort: TRENDING_DESC) { id title { romaji } coverImage { large } averageScore description } } }";
        executeApiCall(query, null, "trending");
    }

    private void fetchRecommended() {
        String query = "query { Page(page: 1, perPage: 20) { media(type: ANIME) { id title { romaji } coverImage { large } averageScore description seasonYear genres } } }";
        executeApiCall(query, null, "home");
    }

    private void searchAnime(String search) {
        progressBar.setVisibility(View.VISIBLE);
        String query = "query ($search: String) { Page(page: 1, perPage: 20) { media(search: $search, type: ANIME) { id title { romaji } coverImage { large } averageScore description seasonYear genres } } }";
        JsonObject variables = new JsonObject();
        variables.addProperty("search", search);
        JsonObject body = new JsonObject();
        body.addProperty("query", query);
        body.add("variables", variables);

        AniListClient.API api = AniListClient.getClient().create(AniListClient.API.class);
        api.query(body).enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                if (response.body() != null) {
                    JsonArray mediaArray = response.body().getAsJsonObject("data").getAsJsonObject("Page").getAsJsonArray("media");
                    if (mediaArray.size() > 0) {
                        tvSearchResults.setVisibility(View.VISIBLE);
                        recyclerSearchResults.setVisibility(View.VISIBLE);
                        recyclerSearchResults.setAdapter(new AnimeAdapter(mediaArray, id -> navigateTo(AnimeDetailsFragment.newInstance(id, false))));
                    }
                }
            }
            @Override public void onFailure(Call<JsonObject> call, Throwable t) { progressBar.setVisibility(View.GONE); }
        });
    }

    private void executeApiCall(String query, JsonObject variables, String target) {
        JsonObject body = new JsonObject();
        body.addProperty("query", query);
        if (variables != null) body.add("variables", variables);

        AniListClient.API api = AniListClient.getClient().create(AniListClient.API.class);
        api.query(body).enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!isAdded() || response.body() == null) return;
                JsonArray mediaArray = response.body().getAsJsonObject("data").getAsJsonObject("Page").getAsJsonArray("media");
                AnimeAdapter adapter = new AnimeAdapter(mediaArray, id -> navigateTo(AnimeDetailsFragment.newInstance(id, false)));
                if ("featured".equals(target)) { adapterFeatured = adapter; recyclerFeatured.setAdapter(adapterFeatured); }
                else if ("trending".equals(target)) { adapterTrending = adapter; recyclerTrending.setAdapter(adapterTrending); }
                else if ("home".equals(target)) { adapterHome = adapter; recyclerHome.setAdapter(adapterHome); }
            }
            @Override public void onFailure(Call<JsonObject> call, Throwable t) {}
        });
    }
}