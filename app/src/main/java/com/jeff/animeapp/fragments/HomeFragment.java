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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
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

        // SORT BUTTONS
        Button btnPopular = v.findViewById(R.id.btnPopular);
        Button btnTop = v.findViewById(R.id.btnTop);
        Button btnTrending = v.findViewById(R.id.btnTrending);

        btnPopular.setOnClickListener(view -> fetchBySort("POPULARITY_DESC"));
        btnTop.setOnClickListener(view -> fetchBySort("SCORE_DESC"));
        btnTrending.setOnClickListener(view -> fetchBySort("TRENDING_DESC"));

        return v;
    }

    // ITO ANG PINAKA-IMPORTANTE NA PART NA INAYOS NATIN
    private void updateAdapter(JsonArray mediaArray) {
        if (mediaArray == null) return;

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            // Kung walang login, diretso lang ipakita lahat
            AnimeAdapter adapter = new AnimeAdapter(mediaArray, false, id -> {
                Fragment detailsFragment = AnimeDetailsFragment.newInstance(id, false);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, detailsFragment)
                        .addToBackStack(null)
                        .commit();
            });
            recyclerView.setAdapter(adapter);
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("watchlist")
                .document(uid)
                .collection("anime")
                .get()
                .addOnSuccessListener((QuerySnapshot snapshot) -> {
                    JsonArray filteredArray = new JsonArray();

                    for (int i = 0; i < mediaArray.size(); i++) {
                        JsonObject anime = mediaArray.get(i).getAsJsonObject();
                        int id = anime.get("id").getAsInt();

                        boolean inWatchlist = snapshot.getDocuments().stream()
                                .anyMatch(doc -> doc.getId().equals(String.valueOf(id)));

                        if (!inWatchlist) {
                            filteredArray.add(anime);
                        }
                    }

                    AnimeAdapter adapter = new AnimeAdapter(filteredArray, false, id -> {
                        Fragment detailsFragment = AnimeDetailsFragment.newInstance(id, false);
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainer, detailsFragment)
                                .addToBackStack(null)
                                .commit();
                    });
                    recyclerView.setAdapter(adapter);
                });
    }

    private void fetchAnimeList() {
        progressBar.setVisibility(View.VISIBLE);
        String query = "query { Page(page: 1, perPage: 20) { media(type: ANIME) { id title { romaji } coverImage { large } averageScore description } } }";

        executeApiCall(query, null);
    }

    private void searchAnime(String search) {
        progressBar.setVisibility(View.VISIBLE);
        String query = "query ($search: String) { Page(page: 1, perPage: 20) { media(search: $search, type: ANIME) { id title { romaji } coverImage { large } averageScore description } } }";

        JsonObject variables = new JsonObject();
        variables.addProperty("search", search);

        executeApiCall(query, variables);
    }

    private void fetchBySort(String sort) {
        progressBar.setVisibility(View.VISIBLE);
        String query = "query ($sort: [MediaSort]) { Page(page: 1, perPage: 20) { media(type: ANIME, sort: $sort) { id title { romaji } coverImage { large } averageScore description } } }";

        JsonObject variables = new JsonObject();
        variables.addProperty("sort", sort);

        executeApiCall(query, variables);
    }

    // Helper para hindi paulit-ulit ang API logic
    private void executeApiCall(String query, JsonObject variables) {
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
                        updateAdapter(mediaArray);
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