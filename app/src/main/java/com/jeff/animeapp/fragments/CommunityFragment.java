package com.jeff.animeapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jeff.animeapp.R;
import com.jeff.animeapp.adapters.CharacterAdapter;
import com.jeff.animeapp.adapters.PostAdapter;
import com.jeff.animeapp.api.KitsuClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private String currentUser;
    private FloatingActionButton btnAddReview;
    private PostAdapter postAdapter;
    private SwipeRefreshLayout swipeRefresh;
    private EditText searchReviews;
    private TextView tvEmptyReviews;

    private LinearLayout layoutReviews, layoutCharacters;
    private MaterialCardView searchSection;
    private TabLayout tabLayout;
    private RecyclerView recyclerCharacters;
    private CharacterAdapter characterAdapter;

    // Firestore Listeners
    private ListenerRegistration reviewsListener;
    private ListenerRegistration charactersListener;

    private Spinner spinnerSort, spinnerRating, spinnerAnime;
    private List<DocumentSnapshot> allReviews = new ArrayList<>();
    private List<DocumentSnapshot> allCharacters = new ArrayList<>();
    private List<String> animeTitles = new ArrayList<>();
    private ArrayAdapter<String> animeAdapter;

    private static final String TAG = "CommunityFragment";

    public CommunityFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_community, container, false);

        recyclerView = v.findViewById(R.id.recyclerCommunity);
        progressBar = v.findViewById(R.id.progressCommunity);
        btnAddReview = v.findViewById(R.id.btnAddReview);

        swipeRefresh = v.findViewById(R.id.swipeRefresh);
        searchReviews = v.findViewById(R.id.searchReviews);
        tvEmptyReviews = v.findViewById(R.id.tvEmptyReviews);

        layoutReviews = v.findViewById(R.id.layoutReviews);
        layoutCharacters = v.findViewById(R.id.layoutCharacters);
        searchSection = v.findViewById(R.id.searchSection);
        tabLayout = v.findViewById(R.id.tabLayout);
        recyclerCharacters = v.findViewById(R.id.recyclerCharacters);

        spinnerSort = v.findViewById(R.id.spinnerSort);
        spinnerRating = v.findViewById(R.id.spinnerRating);
        spinnerAnime = v.findViewById(R.id.spinnerAnime);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerCharacters.setLayoutManager(new LinearLayoutManager(getContext()));

        SharedPreferences userSession = requireActivity()
                .getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUser = userSession.getString("logged_in_user", "Guest");

        Log.d(TAG, "Current user: " + currentUser);

        setupFilters();
        setupSearch();

        swipeRefresh.setOnRefreshListener(() -> {
            if (layoutReviews.getVisibility() == View.VISIBLE) {
                fetchReviews();
            } else {
                fetchCharacters();
            }
        });

        fetchReviews();
        fetchCharacters();

        postAdapter = new PostAdapter(allReviews, currentUser);
        recyclerView.setAdapter(postAdapter);

        characterAdapter = new CharacterAdapter(allCharacters, currentUser);
        recyclerCharacters.setAdapter(characterAdapter);

        generateWeeklyCharacters();

        btnAddReview.setOnClickListener(view -> showAddReviewDialog());

        tabLayout.addTab(tabLayout.newTab().setText("Reviews"));
        tabLayout.addTab(tabLayout.newTab().setText("Top Anime"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switchTab(tab.getPosition() == 0);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        return v;
    }

    private void setupFilters() {
        String[] sortOptions = {"Newest", "Oldest", "Highest Rating", "Lowest Rating"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);

        String[] ratingOptions = {"All Ratings", "5 Stars", "4 Stars", "3 Stars", "2 Stars", "1 Star"};
        ArrayAdapter<String> ratingAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.spinner_item, ratingOptions);
        ratingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRating.setAdapter(ratingAdapter);

        animeTitles.add("All Anime");
        animeAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.spinner_item, animeTitles);
        animeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAnime.setAdapter(animeAdapter);

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerSort.setOnItemSelectedListener(filterListener);
        spinnerRating.setOnItemSelectedListener(filterListener);
        spinnerAnime.setOnItemSelectedListener(filterListener);
    }

    private void setupSearch() {
        if (searchReviews != null) {
            searchReviews.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applyFilters();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void applyFilters() {
        if (allReviews == null || allReviews.isEmpty()) {
            if (postAdapter != null) {
                postAdapter.updateData(new ArrayList<>());
            }
            if (tvEmptyReviews != null) tvEmptyReviews.setVisibility(View.VISIBLE);
            return;
        }

        List<DocumentSnapshot> filteredList = new ArrayList<>(allReviews);
        String ratingFilter = spinnerRating.getSelectedItem().toString();
        if (!ratingFilter.equals("All Ratings")) {
            int targetRating = Integer.parseInt(ratingFilter.substring(0, 1));
            List<DocumentSnapshot> temp = new ArrayList<>();
            for (DocumentSnapshot doc : filteredList) {
                Double rating = doc.getDouble("rating");
                if (rating != null && Math.round(rating) == targetRating) {
                    temp.add(doc);
                }
            }
            filteredList = temp;
        }

        String animeFilter = spinnerAnime.getSelectedItem().toString();
        if (!animeFilter.equals("All Anime")) {
            List<DocumentSnapshot> temp = new ArrayList<>();
            for (DocumentSnapshot doc : filteredList) {
                String title = doc.getString("animeTitle");
                if (title != null && title.equalsIgnoreCase(animeFilter)) {
                    temp.add(doc);
                }
            }
            filteredList = temp;
        }

        String query = searchReviews.getText().toString().trim().toLowerCase();
        if (!query.isEmpty()) {
            List<DocumentSnapshot> temp = new ArrayList<>();
            for (DocumentSnapshot doc : filteredList) {
                String title = doc.getString("animeTitle");
                String username = doc.getString("username");
                String content = doc.getString("reviewText");

                boolean matches = (title != null && title.toLowerCase().contains(query)) ||
                                  (username != null && username.toLowerCase().contains(query)) ||
                                  (content != null && content.toLowerCase().contains(query));

                if (matches) temp.add(doc);
            }
            filteredList = temp;
        }

        String sortOrder = spinnerSort.getSelectedItem().toString();
        Collections.sort(filteredList, (d1, d2) -> {
            switch (sortOrder) {
                case "Newest":
                    return Long.compare(getTimestamp(d2), getTimestamp(d1));
                case "Oldest":
                    return Long.compare(getTimestamp(d1), getTimestamp(d2));
                case "Highest Rating":
                    return Double.compare(getRating(d2), getRating(d1));
                case "Lowest Rating":
                    return Double.compare(getRating(d1), getRating(d2));
                default:
                    return 0;
            }
        });

        if (tvEmptyReviews != null) {
            tvEmptyReviews.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        }

        if (postAdapter != null) {
            postAdapter.updateData(filteredList);
        }
    }

    private long getTimestamp(DocumentSnapshot doc) {
        Long t = doc.getLong("timestamp");
        return (t != null) ? t : 0L;
    }

    private double getRating(DocumentSnapshot doc) {
        Double r = doc.getDouble("rating");
        return (r != null) ? r : 0.0;
    }

    private void switchTab(boolean isReviews) {
        if (isReviews) {
            layoutReviews.setVisibility(View.VISIBLE);
            layoutCharacters.setVisibility(View.GONE);
            searchSection.setVisibility(View.VISIBLE);
            btnAddReview.setVisibility(View.VISIBLE);

            fetchReviews();
        } else {
            layoutReviews.setVisibility(View.GONE);
            layoutCharacters.setVisibility(View.VISIBLE);
            searchSection.setVisibility(View.GONE);
            btnAddReview.setVisibility(View.GONE);

            fetchCharacters();
        }
    }

    private void fetchReviews() {
        if (!swipeRefresh.isRefreshing()) progressBar.setVisibility(View.VISIBLE);

        if (reviewsListener != null) {
            reviewsListener.remove();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        reviewsListener = db.collection("reviews")
                .addSnapshotListener((snapshots, e) -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);

                    if (e != null) {
                        Log.e(TAG, "Error fetching reviews", e);
                        Toast.makeText(getContext(),
                                "Error loading reviews: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (snapshots != null) {
                        allReviews = snapshots.getDocuments();

                        Set<String> titles = new TreeSet<>();
                        for (DocumentSnapshot doc : allReviews) {
                            String title = doc.getString("animeTitle");
                            if (title != null) titles.add(title);
                        }

                        animeTitles.clear();
                        animeTitles.add("All Anime");
                        animeTitles.addAll(titles);
                        if (animeAdapter != null) {
                            animeAdapter.notifyDataSetChanged();
                        }

                        applyFilters();
                    } else {
                        Log.w(TAG, "Snapshots is null");
                        allReviews = new ArrayList<>();
                        applyFilters();
                    }
                });
    }

    private void fetchCharacters() {
        if (!swipeRefresh.isRefreshing()) progressBar.setVisibility(View.VISIBLE);

        if (charactersListener != null) {
            charactersListener.remove();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d(TAG, "Fetching characters");

        charactersListener = db.collection("characters")
                .orderBy("votes", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);

                    if (e != null) {
                        Log.e(TAG, "Error fetching characters", e);
                        Toast.makeText(getContext(),
                                "Error loading characters: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        Log.d(TAG, "Found " + snapshots.size() + " characters");
                        allCharacters = snapshots.getDocuments();
                        if (characterAdapter != null) characterAdapter.updateData(allCharacters);
                        updateVoteCountDisplay();
                    } else {
                        Log.w(TAG, "No characters found");
                        allCharacters = new ArrayList<>();
                        if (characterAdapter != null) characterAdapter.updateData(allCharacters);
                        updateVoteCountDisplay();
                    }
                });
    }

    private Map<String, String> searchResultMap = new HashMap<>();
    private String selectedAnimeId = null;

    private void showAddReviewDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_review, null);

        AutoCompleteTextView editAnimeTitle = dialogView.findViewById(R.id.editAnimeTitle);
        EditText editReview = dialogView.findViewById(R.id.editReview);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);

        selectedAnimeId = null;
        searchResultMap.clear();

        editAnimeTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                selectedAnimeId = null;
                if (s.length() >= 3) {
                    searchAnimeTitles(s.toString(), editAnimeTitle);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editAnimeTitle.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTitle = (String) parent.getItemAtPosition(position);
            selectedAnimeId = searchResultMap.get(selectedTitle);
            Log.d(TAG, "Selected Anime: " + selectedTitle + " ID: " + selectedAnimeId);
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.AnimeAlertDialog)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSubmit.setOnClickListener(v -> {
            String animeTitle = editAnimeTitle.getText().toString().trim();
            String reviewText = editReview.getText().toString().trim();
            float rating = ratingBar.getRating();

            if (animeTitle.isEmpty() || reviewText.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedAnimeId == null) {
                Toast.makeText(getContext(), "Please select a valid anime from the suggestions!", Toast.LENGTH_SHORT).show();
                return;
            }

            submitReview(animeTitle, reviewText, rating, selectedAnimeId);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void searchAnimeTitles(String query, AutoCompleteTextView textView) {
        KitsuClient.API api = KitsuClient.getClient().create(KitsuClient.API.class);
        api.searchAnime(query).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> titles = new ArrayList<>();
                    JsonArray data = response.body().getAsJsonArray("data");
                    
                    searchResultMap.clear();
                    
                    if (data != null) {
                        for (JsonElement element : data) {
                            JsonObject anime = element.getAsJsonObject();
                            String id = anime.get("id").getAsString();
                            JsonObject attributes = anime.getAsJsonObject("attributes");
                            if (attributes != null) {
                                String title = attributes.get("canonicalTitle").getAsString();
                                titles.add(title);
                                searchResultMap.put(title, id);
                            }
                        }
                    }

                    if (getContext() != null) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                                android.R.layout.simple_dropdown_item_1line, titles);
                        textView.setAdapter(adapter);
                        textView.showDropDown();
                        
                        String currentText = textView.getText().toString();
                        if (searchResultMap.containsKey(currentText)) {
                            selectedAnimeId = searchResultMap.get(currentText);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "Anime search failed", t);
            }
        });
    }

    private void submitReview(String animeTitle, String reviewText, float rating, String animeId) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> review = new HashMap<>();
        review.put("animeId", animeId);
        review.put("animeTitle", animeTitle);
        review.put("reviewText", reviewText);
        review.put("rating", rating);
        review.put("username", currentUser);
        review.put("timestamp", System.currentTimeMillis());

        Log.d(TAG, "Submitting review: " + animeTitle + " (ID: " + animeId + ") by " + currentUser);

        db.collection("reviews")
                .add(review)
                .addOnSuccessListener(docRef -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Review submitted!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Review added with ID: " + docRef.getId());
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Submit error", e);
                });
    }

    private Map<String, Object> createCharacter(String name, String anime, String imageUrl) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("anime", anime);
        map.put("imageUrl", imageUrl);
        map.put("votes", 0L);
        map.put("voters", new ArrayList<String>());
        return map;
    }

    private void generateWeeklyCharacters() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("WeeklyCharacters", Context.MODE_PRIVATE);

        long lastUpdate = prefs.getLong("last_update_v7", 0);
        long now = System.currentTimeMillis();

        if (now - lastUpdate < 604800000 && lastUpdate != 0) {
            db.collection("characters").get().addOnSuccessListener(snapshots -> {
                if (snapshots.isEmpty()) {
                    fetchCharactersFromApi(db, prefs, now);
                }
            });
            Log.d(TAG, "Weekly characters already generated this week");
            return;
        }

        fetchCharactersFromApi(db, prefs, now);
    }

    private void fetchCharactersFromApi(FirebaseFirestore db, SharedPreferences prefs, long now) {
        Log.d(TAG, "Fetching trending anime from Kitsu API...");

        KitsuClient.API apiService = KitsuClient.getClient().create(KitsuClient.API.class);

        apiService.getTrendingAnime().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonArray data = response.body().getAsJsonArray("data");
                    if (data != null && data.size() > 0) {
                        db.collection("characters").get().addOnSuccessListener(snapshots -> {
                            // Clear existing
                            for (DocumentSnapshot doc : snapshots) {
                                doc.getReference().delete();
                            }

                            int count = Math.min(data.size(), 10);
                            for (int i = 0; i < count; i++) {
                                JsonObject animeObj = data.get(i).getAsJsonObject();
                                JsonObject attributes = animeObj.getAsJsonObject("attributes");
                                
                                String title = attributes.get("canonicalTitle").getAsString();
                                String posterUrl = "";
                                if (attributes.has("posterImage") && !attributes.get("posterImage").isJsonNull()) {
                                    posterUrl = attributes.getAsJsonObject("posterImage").get("medium").getAsString();
                                }
                                
                                String type = attributes.has("showType") ? attributes.get("showType").getAsString() : "TV";

                                Map<String, Object> animeData = new HashMap<>();
                                animeData.put("name", title);
                                animeData.put("anime", type.toUpperCase());
                                animeData.put("imageUrl", posterUrl);
                                animeData.put("votes", 0L);
                                animeData.put("voters", new ArrayList<String>());
                                db.collection("characters").add(animeData);
                            }

                            prefs.edit().putLong("last_update_v7", now).apply();
                            Log.d(TAG, "Successfully populated trending anime");
                        });
                    } else {
                        forceGenerateCharacters(db, prefs, now);
                    }
                } else {
                    forceGenerateCharacters(db, prefs, now);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "API Failure: " + t.getMessage());
                forceGenerateCharacters(db, prefs, now);
            }
        });
    }

    private void forceGenerateCharacters(FirebaseFirestore db, SharedPreferences prefs, long now) {
        List<Map<String, Object>> fallbackAnimes = new ArrayList<>();
        fallbackAnimes.add(createCharacter("Naruto Shippuden", "TV", "https://media.kitsu.io/anime/poster_images/1555/medium.jpg"));
        fallbackAnimes.add(createCharacter("Jujutsu Kaisen", "TV", "https://media.kitsu.io/anime/poster_images/42765/medium.jpg"));
        fallbackAnimes.add(createCharacter("One Piece", "TV", "https://media.kitsu.io/anime/poster_images/12/medium.jpg"));
        fallbackAnimes.add(createCharacter("Attack on Titan", "TV", "https://media.kitsu.io/anime/poster_images/7442/medium.jpg"));
        fallbackAnimes.add(createCharacter("Demon Slayer", "TV", "https://media.kitsu.io/anime/poster_images/41370/medium.jpg"));
        fallbackAnimes.add(createCharacter("Ponyo", "MOVIE", "https://media.kitsu.io/anime/poster_images/3114/medium.jpg"));
        fallbackAnimes.add(createCharacter("My Hero Academia", "TV", "https://media.kitsu.io/anime/poster_images/11467/medium.jpg"));
        fallbackAnimes.add(createCharacter("Death Note", "TV", "https://media.kitsu.io/anime/poster_images/1376/medium.jpg"));
        fallbackAnimes.add(createCharacter("Hunter x Hunter", "TV", "https://media.kitsu.io/anime/poster_images/6448/medium.jpg"));
        fallbackAnimes.add(createCharacter("Fullmetal Alchemist: B", "TV", "https://media.kitsu.io/anime/poster_images/3936/medium.jpg"));

        db.collection("characters").get().addOnSuccessListener(snapshots -> {
            for (DocumentSnapshot doc : snapshots) {
                doc.getReference().delete();
            }
            for (Map<String, Object> anime : fallbackAnimes) {
                db.collection("characters").add(anime);
            }
            prefs.edit().putLong("last_update_v7", now).apply();
            Log.d(TAG, "Successfully populated fallback anime");
        });
    }

    private void updateVoteCountDisplay() {
        if (getView() != null) {
            TextView voteLimitText = getView().findViewById(R.id.voteLimitText);
            if (voteLimitText != null && characterAdapter != null) {
                int votesUsed = characterAdapter.getUserVoteCount();
                voteLimitText.setText("Votes this week: " + votesUsed + "/3");
                if (votesUsed >= 3) {
                    voteLimitText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                    voteLimitText.setText("Votes this week: " + votesUsed + "/3 (Limit Reached)");
                } else {
                    voteLimitText.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent));
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (reviewsListener != null) {
            reviewsListener.remove();
            reviewsListener = null;
        }
        if (charactersListener != null) {
            charactersListener.remove();
            charactersListener = null;
        }
    }
}