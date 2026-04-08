package com.jeff.animeapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
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

    private AnimeAdapter adapterFeatured, adapterTrending, adapterHome;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerFeatured = v.findViewById(R.id.recyclerFeatured);
        recyclerTrending = v.findViewById(R.id.recyclerTrending);
        recyclerHome = v.findViewById(R.id.recyclerHome);
        progressBar = v.findViewById(R.id.progressHome);
        searchInput = v.findViewById(R.id.searchInput);

        // Layout managers
        recyclerFeatured.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerTrending.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerHome.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));


        // Fetch each section
        fetchFeatured();
        fetchTrending();
        fetchRecommended();

        // SEARCH
        searchInput.setOnEditorActionListener((textView, i, keyEvent) -> {
            String searchText = searchInput.getText().toString();
            if (!searchText.isEmpty()) {
                searchAnime(searchText);
            }
            return true;
        });

        // FILTER ICON
        ImageView filterIcon = v.findViewById(R.id.ic_filter);
        filterIcon.setOnClickListener(view -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showFilterDialog(this);
            }
        });

        return v;
    }

    // 🔑 FILTERS (Recommended section only)
    public void applyFilters(List<String> genres, List<String> years) {
        if (adapterHome == null || adapterHome.getMediaArray() == null) return;

        JsonArray filteredArray = new JsonArray();

        for (int i = 0; i < adapterHome.getMediaArray().size(); i++) {
            JsonObject anime = adapterHome.getMediaArray().get(i).getAsJsonObject();
            String genre = anime.has("genres") ? anime.getAsJsonArray("genres").toString() : "";
            String year = anime.has("seasonYear") ? anime.get("seasonYear").getAsString() : "";

            boolean matchGenre = genres.isEmpty() || genres.stream().anyMatch(genre::contains);
            boolean matchYear = years.isEmpty() || years.contains(year);

            if (matchGenre && matchYear) {
                filteredArray.add(anime);
            }
        }

        adapterHome.updateData(filteredArray);
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

        executeApiCall(query, variables, "home");
    }

    private void executeApiCall(String query, JsonObject variables, String target) {
        JsonObject body = new JsonObject();
        body.addProperty("query", query);
        if (variables != null) body.add("variables", variables);

        AniListClient.API api = AniListClient.getClient().create(AniListClient.API.class);
        api.query(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);

                try {
                    if (response.body() != null) {
                        JsonArray mediaArray = response.body()
                                .getAsJsonObject("data")
                                .getAsJsonObject("Page")
                                .getAsJsonArray("media");

                        AnimeAdapter adapter = new AnimeAdapter(mediaArray, id -> {
                            Fragment detailsFragment = AnimeDetailsFragment.newInstance(id, false);
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragmentContainer, detailsFragment)
                                    .addToBackStack(null)
                                    .commit();
                        });

                        switch (target) {
                            case "featured":
                                adapterFeatured = adapter;
                                recyclerFeatured.setAdapter(adapterFeatured);
                                break;
                            case "trending":
                                adapterTrending = adapter;
                                recyclerTrending.setAdapter(adapterTrending);
                                break;
                            case "home":
                                adapterHome = adapter;
                                recyclerHome.setAdapter(adapterHome);
                                break;
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error loading data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "API Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
