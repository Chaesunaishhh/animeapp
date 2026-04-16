package com.jeff.animeapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

    private RecyclerView recyclerFeatured, recyclerTrending, recyclerHome, recyclerMyList;
    private ProgressBar progressBar;
    private EditText searchInput;
    private ImageView filterIcon, searchIcon, btnBack;
    private RecyclerView recyclerSearchResults;
    private TextView tvSearchResults, tvHeaderTitle, tvHeaderSubtitle;
    private TextView btnSeeAllRecommended, btnSeeAllFeatured, btnSeeAllTrending, btnSeeAllMyList;
    private AnimeAdapter adapterFeatured, adapterTrending, adapterHome, adapterMyList;
    private JsonArray dataRecommended, dataFeatured, dataTrending, dataMyList;
    private View layoutQuickAccess, layoutMainSections, layoutMyList;

    public HomeFragment() {}

    private java.util.Set<Integer> watchlistIds = new java.util.HashSet<>();

    private void fetchWatchlistIds() {
        String uid = com.jeff.animeapp.utils.FirebaseUtils.uid();
        if (uid == null) return;

        com.jeff.animeapp.utils.FirebaseUtils.firestore()
                .collection("watchlist")
                .document(uid)
                .collection("anime")
                .get()
                .addOnSuccessListener(snapshot -> {
                    watchlistIds.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                        Long id = doc.getLong("id");
                        if (id != null) watchlistIds.add(id.intValue());
                    }
                    updateAdaptersWatchlist();
                });
    }

    private void fetchMyList() {
        String uid = com.jeff.animeapp.utils.FirebaseUtils.uid();
        if (uid == null) return;

        com.jeff.animeapp.utils.FirebaseUtils.firestore()
                .collection("watchlist")
                .document(uid)
                .collection("anime")
                .whereIn("status", java.util.Arrays.asList("watching", "completed"))
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) return;
                    JsonArray mediaArray = new JsonArray();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                        JsonObject obj = new JsonObject();
                        obj.addProperty("id", doc.getLong("id"));
                        JsonObject titleObj = new JsonObject();
                        titleObj.addProperty("romaji", doc.getString("title"));
                        obj.add("title", titleObj);
                        JsonObject imgObj = new JsonObject();
                        imgObj.addProperty("large", doc.getString("coverImage"));
                        obj.add("coverImage", imgObj);
                        
                        // We need score for the adapter to display it correctly
                        obj.addProperty("averageScore", doc.getLong("score"));
                        
                        mediaArray.add(obj);
                    }

                    if (mediaArray.size() > 0) {
                        dataMyList = mediaArray;
                        layoutMyList.setVisibility(View.VISIBLE);
                        adapterMyList = new AnimeAdapter(mediaArray, id -> 
                            navigateTo(AnimeDetailsFragment.newInstance(id, true)));
                        adapterMyList.setWatchlistIds(watchlistIds);
                        recyclerMyList.setAdapter(adapterMyList);
                    } else {
                        layoutMyList.setVisibility(View.GONE);
                    }
                });
    }

    private void updateAdaptersWatchlist() {
        if (adapterFeatured != null) adapterFeatured.setWatchlistIds(watchlistIds);
        if (adapterTrending != null) adapterTrending.setWatchlistIds(watchlistIds);
        if (adapterHome != null) adapterHome.setWatchlistIds(watchlistIds);
        if (adapterMyList != null) adapterMyList.setWatchlistIds(watchlistIds);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        
        searchInput = v.findViewById(R.id.searchInput);
        filterIcon = v.findViewById(R.id.ic_filter);
        searchIcon = v.findViewById(R.id.ic_search);
        btnBack = v.findViewById(R.id.btnBack);

        recyclerFeatured = v.findViewById(R.id.recyclerFeatured);
        recyclerTrending = v.findViewById(R.id.recyclerTrending);
        recyclerHome = v.findViewById(R.id.recyclerHome);
        recyclerSearchResults = v.findViewById(R.id.recyclerSearchResults);

        tvSearchResults = v.findViewById(R.id.tvSearchResults);
        tvHeaderTitle = v.findViewById(R.id.tvHeaderTitle);
        tvHeaderSubtitle = v.findViewById(R.id.tvHeaderSubtitle);

        btnSeeAllRecommended = v.findViewById(R.id.btnSeeAllRecommended);
        btnSeeAllFeatured = v.findViewById(R.id.btnSeeAllFeatured);
        btnSeeAllTrending = v.findViewById(R.id.btnSeeAllTrending);

        progressBar = v.findViewById(R.id.progressHome);
        layoutQuickAccess = v.findViewById(R.id.layoutQuickAccess);
        layoutMainSections = v.findViewById(R.id.layoutMainSections);
        layoutMyList = v.findViewById(R.id.layoutMyList);
        recyclerMyList = v.findViewById(R.id.recyclerMyList);
        btnSeeAllMyList = v.findViewById(R.id.btnSeeAllMyList);

        recyclerFeatured.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerTrending.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerHome.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerMyList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerSearchResults.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(getContext(), 2));

        fetchFeatured();
        fetchTrending();
        fetchRecommended();
        fetchMyList();
        fetchWatchlistIds();

        // Search Logic
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    btnBack.setVisibility(View.VISIBLE);
                    layoutQuickAccess.setVisibility(View.GONE);
                    layoutMainSections.setVisibility(View.GONE);
                    searchAnime(query);
                } else {
                    resetToHome();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter Icon
        filterIcon.setOnClickListener(view -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showFilterDialog(this);
            }
        });

        // ✅ QUICK ACCESS: CALENDAR
        View btnCalendar = v.findViewById(R.id.btnCalendar);
        ((TextView) btnCalendar.findViewById(R.id.tvLabel)).setText("CALENDAR");
        ((ImageView) btnCalendar.findViewById(R.id.ivIcon)).setImageResource(R.drawable.ic_calendar);
        btnCalendar.setOnClickListener(view ->
                navigateTo(new ReleaseCalendarFragment()));

        // ✅ QUICK ACCESS: QUIZ
        View btnQuiz = v.findViewById(R.id.btnQuiz);
        ((TextView) btnQuiz.findViewById(R.id.tvLabel)).setText("QUIZ");
        ((ImageView) btnQuiz.findViewById(R.id.ivIcon)).setImageResource(R.drawable.ic_quiz);
        btnQuiz.setOnClickListener(view ->
                navigateTo(new QuizFragment()));

        // ✅ QUICK ACCESS: LEADERBOARD
        View btnLeaderboard = v.findViewById(R.id.btnLeaderboard);
        ((TextView) btnLeaderboard.findViewById(R.id.tvLabel)).setText("RANKS");
        ((ImageView) btnLeaderboard.findViewById(R.id.ivIcon)).setImageResource(R.drawable.ic_leaderboard);
        btnLeaderboard.setOnClickListener(view ->
                navigateTo(new LeaderboardFragment()));

        // See All Click Listeners
        btnSeeAllRecommended.setOnClickListener(view -> showVerticalList("Recommended", dataRecommended));
        btnSeeAllFeatured.setOnClickListener(view -> showVerticalList("Featured", dataFeatured));
        btnSeeAllTrending.setOnClickListener(view -> showVerticalList("Trending", dataTrending));
        btnSeeAllMyList.setOnClickListener(view -> showVerticalList("My List", dataMyList));

        v.findViewById(R.id.btnNotificationsHome).setOnClickListener(view -> {
            navigateTo(new NotificationsFragment());
        });

        // Back Button Logic
        btnBack.setOnClickListener(view -> {
            resetToHome();
        });

        return v;
    }

    private void resetToHome() {
        btnBack.setVisibility(View.GONE);
        tvHeaderTitle.setText(R.string.app_title);
        tvHeaderSubtitle.setText(R.string.discover_text);
        tvHeaderSubtitle.setVisibility(View.VISIBLE);
        
        layoutQuickAccess.setVisibility(View.VISIBLE);
        layoutMainSections.setVisibility(View.VISIBLE);
        if (dataMyList != null && dataMyList.size() > 0) {
            layoutMyList.setVisibility(View.VISIBLE);
        }
        tvSearchResults.setVisibility(View.GONE);
        tvSearchResults.setText(R.string.search_results);
        recyclerSearchResults.setVisibility(View.GONE);

        // Prevent infinite loop if called from TextWatcher
        if (searchInput.getText().length() > 0) {
            searchInput.setText("");
        }
    }

    private void showVerticalList(String title, JsonArray data) {
        if (data == null || data.size() == 0) return;

        btnBack.setVisibility(View.VISIBLE);
        tvHeaderTitle.setText(title);
        tvHeaderSubtitle.setVisibility(View.GONE);

        tvSearchResults.setVisibility(View.GONE); // Hide the middle text view since it's in header now
        recyclerSearchResults.setVisibility(View.VISIBLE);
        
        AnimeAdapter adapter = new AnimeAdapter(data, true,
                id -> navigateTo(AnimeDetailsFragment.newInstance(id, false)));
        adapter.setWatchlistIds(watchlistIds);
        recyclerSearchResults.setAdapter(adapter);

        layoutQuickAccess.setVisibility(View.GONE);
        layoutMainSections.setVisibility(View.GONE);
        layoutMyList.setVisibility(View.GONE);
    }

    // ✅ ADDED THIS METHOD TO FIX MAINACTIVITY ERROR
    public void applyFilters(List<String> genres, List<String> years) {
        if (genres.isEmpty() && years.isEmpty()) {
            layoutQuickAccess.setVisibility(View.VISIBLE);
            layoutMainSections.setVisibility(View.VISIBLE);
            tvSearchResults.setVisibility(View.GONE);
            recyclerSearchResults.setVisibility(View.GONE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String query = "query ($genres: [String], $year: Int) { " +
                "Page(page: 1, perPage: 20) { " +
                "media(type: ANIME, genre_in: $genres, seasonYear: $year) { " +
                "id title { romaji } coverImage { large } averageScore description seasonYear genres } } }";

        JsonObject variables = new JsonObject();
        if (!genres.isEmpty()) {
            JsonArray genreArray = new JsonArray();
            for (String g : genres) genreArray.add(g);
            variables.add("genres", genreArray);
        }
        if (!years.isEmpty()) {
            try {
                int year = Integer.parseInt(years.get(0));
                variables.addProperty("year", year);
            } catch (NumberFormatException e) {
            }
        }

        JsonObject body = new JsonObject();
        body.addProperty("query", query);
        body.add("variables", variables);

        AniListClient.API api = AniListClient.getClient().create(AniListClient.API.class);
        api.query(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                if (response.body() != null) {
                    JsonArray mediaArray = response.body()
                            .getAsJsonObject("data")
                            .getAsJsonObject("Page")
                            .getAsJsonArray("media");

                    if (mediaArray.size() > 0) {
                        tvSearchResults.setText(R.string.search_results);
                        tvSearchResults.setVisibility(View.VISIBLE);
                        recyclerSearchResults.setVisibility(View.VISIBLE);
                        
                        AnimeAdapter adapter = new AnimeAdapter(mediaArray, true,
                                id -> navigateTo(AnimeDetailsFragment.newInstance(id, false)));
                        adapter.setWatchlistIds(watchlistIds);
                        recyclerSearchResults.setAdapter(adapter);

                        // Hide other sections
                        layoutQuickAccess.setVisibility(View.GONE);
                        layoutMainSections.setVisibility(View.GONE);
                    } else {
                        tvSearchResults.setText("No results found");
                        tvSearchResults.setVisibility(View.VISIBLE);
                        recyclerSearchResults.setVisibility(View.GONE);
                        layoutQuickAccess.setVisibility(View.GONE);
                        layoutMainSections.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to fetch filtered anime", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateTo(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void fetchFeatured() {
        String query = "query { Page(page: 1, perPage: 50) { media(type: ANIME, sort: POPULARITY_DESC, genre_in: [\"Slice of Life\", \"Comedy\"]) { id title { romaji } coverImage { large } averageScore description } } }";
        executeApiCall(query, null, "featured");
    }

    private void fetchTrending() {
        String query = "query { Page(page: 1, perPage: 50) { media(type: ANIME, sort: TRENDING_DESC) { id title { romaji } coverImage { large } averageScore description } } }";
        executeApiCall(query, null, "trending");
    }

    private void fetchRecommended() {
        String query = "query { Page(page: 1, perPage: 50) { media(type: ANIME, sort: SCORE_DESC) { id title { romaji } coverImage { large } averageScore description seasonYear genres } } }";
        executeApiCall(query, null, "home");
    }

    private void searchAnime(String search) {
        if (search.isEmpty()) {
            // Restore sections if search is cleared
            layoutQuickAccess.setVisibility(View.VISIBLE);
            layoutMainSections.setVisibility(View.VISIBLE);
            tvSearchResults.setVisibility(View.GONE);
            recyclerSearchResults.setVisibility(View.GONE);
            return;
        }

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
                        tvSearchResults.setText(R.string.search_results);
                        tvSearchResults.setVisibility(View.VISIBLE);
                        recyclerSearchResults.setVisibility(View.VISIBLE);
                        
                        AnimeAdapter adapter = new AnimeAdapter(mediaArray, true, id -> navigateTo(AnimeDetailsFragment.newInstance(id, false)));
                        adapter.setWatchlistIds(watchlistIds);
                        recyclerSearchResults.setAdapter(adapter);
                    } else {
                        tvSearchResults.setText("No results found");
                        tvSearchResults.setVisibility(View.VISIBLE);
                        recyclerSearchResults.setVisibility(View.GONE);
                        layoutQuickAccess.setVisibility(View.GONE);
                        layoutMainSections.setVisibility(View.GONE);
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
                JsonArray mediaArray = response.body().getAsJsonObject("data")
                        .getAsJsonObject("Page")
                        .getAsJsonArray("media");

                AnimeAdapter adapter = new AnimeAdapter(mediaArray,
                        id -> navigateTo(AnimeDetailsFragment.newInstance(id, false)));
                adapter.setWatchlistIds(watchlistIds);

                if ("featured".equals(target)) {
                    dataFeatured = mediaArray;
                    adapterFeatured = adapter;
                    recyclerFeatured.setAdapter(adapterFeatured);
                } else if ("trending".equals(target)) {
                    dataTrending = mediaArray;
                    adapterTrending = adapter;
                    recyclerTrending.setAdapter(adapterTrending);
                } else if ("home".equals(target)) {
                    dataRecommended = mediaArray;
                    adapterHome = adapter;
                    recyclerHome.setAdapter(adapterHome);
                }
            }

            @Override public void onFailure(Call<JsonObject> call, Throwable t) {
                // Optional: show error message
                Toast.makeText(getContext(), "Failed to load " + target, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
