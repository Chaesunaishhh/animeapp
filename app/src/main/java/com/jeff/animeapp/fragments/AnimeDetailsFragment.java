package com.jeff.animeapp.fragments;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.jeff.animeapp.adapters.AnimeAdapter;
import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jeff.animeapp.R;
import com.jeff.animeapp.api.AniListClient;
import com.jeff.animeapp.utils.FirebaseUtils;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.jeff.animeapp.notifications.NotificationHelper;
import java.util.HashMap;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnimeDetailsFragment extends Fragment {

    private ImageView cover, btnBack;
    private TextView title, score, description, meta, characters, tvRelatedTitle;
    private MaterialButton btnWatchlist, btnComplete, btnRemove;
    private RecyclerView rvRecommendations;
    private AnimeAdapter recommendationsAdapter;
    private View layoutWatchlistActions;
    private String imageUrl = "";
    private boolean isFromWatchlist = false;

    public static AnimeDetailsFragment newInstance(int id, boolean isWatchlist) {
        AnimeDetailsFragment fragment = new AnimeDetailsFragment();
        Bundle args = new Bundle();
        args.putInt("anime_id", id);
        args.putBoolean("is_watchlist", isWatchlist);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_anime_details, container, false);

        cover = v.findViewById(R.id.animeCover);
        btnBack = v.findViewById(R.id.btnBack);
        title = v.findViewById(R.id.animeTitle);
        score = v.findViewById(R.id.animeScore);
        description = v.findViewById(R.id.animeDescription);
        meta = v.findViewById(R.id.animeMeta);
        characters = v.findViewById(R.id.animeCharacters);
        btnWatchlist = v.findViewById(R.id.btnWatchlist);
        btnComplete = v.findViewById(R.id.btnDetailsComplete);
        btnRemove = v.findViewById(R.id.btnDetailsRemove);
        layoutWatchlistActions = v.findViewById(R.id.layoutDetailsActions);
        rvRecommendations = v.findViewById(R.id.rvRecommendations);
        tvRelatedTitle = v.findViewById(R.id.tvRelatedTitle);

        rvRecommendations.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recommendationsAdapter = new AnimeAdapter(new JsonArray(), id -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, AnimeDetailsFragment.newInstance(id, false))
                    .addToBackStack(null)
                    .commit();
        });
        rvRecommendations.setAdapter(recommendationsAdapter);

        if (getArguments() != null) {
            int animeId = getArguments().getInt("anime_id");
            isFromWatchlist = getArguments().getBoolean("is_watchlist");

            fetchAnimeDetails(animeId);
            checkStatus(animeId);

            if (isFromWatchlist) {
                layoutWatchlistActions.setVisibility(View.VISIBLE);
            } else {
                btnWatchlist.setVisibility(View.VISIBLE);
                layoutWatchlistActions.setVisibility(View.GONE);
            }

            btnWatchlist.setOnClickListener(view -> addToWatchlist(animeId));
            btnRemove.setOnClickListener(view -> removeFromWatchlist(animeId));
            btnComplete.setOnClickListener(view -> updateStatusToCompleted(animeId));
        }

        btnBack.setOnClickListener(view -> {
            getParentFragmentManager().popBackStack();
        });

        return v;
    }

    private void fetchAnimeDetails(int id) {
        String query = "query ($id: Int) { Media(id: $id, type: ANIME) { " +
                "id title { romaji } description coverImage { large } averageScore " +
                "seasonYear episodes genres " +
                "characters { edges { node { name { full } } } } " +
                "recommendations(perPage: 10, sort: RATING_DESC) { nodes { mediaRecommendation { id title { romaji } coverImage { large } averageScore } } } } }";

        JsonObject variables = new JsonObject();
        variables.addProperty("id", id);

        JsonObject body = new JsonObject();
        body.addProperty("query", query);
        body.add("variables", variables);

        AniListClient.getClient().create(AniListClient.API.class).query(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!isAdded()) return;
                if (response.body() != null) {
                    JsonObject media = response.body().getAsJsonObject("data").getAsJsonObject("Media");

                    String titleStr = media.getAsJsonObject("title").get("romaji").getAsString();
                    String descStr = media.has("description") && !media.get("description").isJsonNull()
                            ? media.get("description").getAsString().replaceAll("<.*?>", "")
                            : "No description available.";
                    imageUrl = media.getAsJsonObject("coverImage").get("large").getAsString();
                    int scoreInt = media.has("averageScore") && !media.get("averageScore").isJsonNull()
                            ? media.get("averageScore").getAsInt() : 0;

                    int year = media.has("seasonYear") && !media.get("seasonYear").isJsonNull()
                            ? media.get("seasonYear").getAsInt() : 0;
                    int episodes = media.has("episodes") && !media.get("episodes").isJsonNull()
                            ? media.get("episodes").getAsInt() : 0;
                    String genresStr = "";
                    if (media.has("genres")) {
                        genresStr = media.getAsJsonArray("genres").toString()
                                .replace("[", "")
                                .replace("]", "")
                                .replace("\"", "");
                    }

                    // Characters
                    StringBuilder charsBuilder = new StringBuilder();
                    if (media.has("characters")) {
                        JsonArray charEdges = media.getAsJsonObject("characters").getAsJsonArray("edges");
                        for (int i = 0; i < charEdges.size(); i++) {
                            JsonObject node = charEdges.get(i).getAsJsonObject().getAsJsonObject("node");
                            String name = node.getAsJsonObject("name").get("full").getAsString();
                            charsBuilder.append(name);
                            if (i < charEdges.size() - 1) charsBuilder.append(", ");
                        }
                    }

                    // Bind to UI
                    title.setText(titleStr);
                    description.setText(descStr);
                    score.setText("⭐ " + (scoreInt / 10.0));
                    meta.setText(year + " • " + genresStr + " • " + episodes + " Episodes");
                    characters.setText(charsBuilder.length() > 0 ? charsBuilder.toString() : "No characters available.");

                    // Recommendations
                    if (media.has("recommendations")) {
                        JsonArray nodes = media.getAsJsonObject("recommendations").getAsJsonArray("nodes");
                        JsonArray recList = new JsonArray();
                        for (int i = 0; i < nodes.size(); i++) {
                            JsonObject recNode = nodes.get(i).getAsJsonObject().getAsJsonObject("mediaRecommendation");
                            if (recNode != null) {
                                recList.add(recNode);
                            }
                        }
                        if (recList.size() > 0) {
                            tvRelatedTitle.setVisibility(View.VISIBLE);
                            recommendationsAdapter.updateData(recList);
                        } else {
                            tvRelatedTitle.setVisibility(View.GONE);
                        }
                    }

                    Glide.with(requireContext())
                            .load(imageUrl)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.placeholder_image)
                            .into(cover);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Failed to load details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkStatus(int id) {
        String uid = FirebaseUtils.uid();
        if (uid == null) return;

        FirebaseFirestore.getInstance().collection("watchlist").document(uid)
                .collection("anime").document(String.valueOf(id)).get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) return;
                    if (doc.exists()) {
                        String status = doc.getString("status");
                        if ("completed".equals(status)) {
                            btnComplete.setText("COMPLETED");
                            btnComplete.setEnabled(false);
                            btnComplete.setAlpha(0.6f);
                        } else if ("watching".equals(status)) {
                            btnWatchlist.setText("Watching");
                            btnWatchlist.setEnabled(false);
                            btnWatchlist.setAlpha(0.8f);
                            btnWatchlist.setIconResource(R.drawable.ic_check);
                        }
                    }
                });
    }

    private void addToWatchlist(int id) {
        String uid = FirebaseUtils.uid();
        if (uid == null) return;

        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("title", title.getText().toString());
        map.put("coverImage", imageUrl);
        map.put("description", description.getText().toString());

        String scoreText = score.getText().toString().replace("⭐ ", "");
        try {
            double dScore = Double.parseDouble(scoreText.trim());
            map.put("score", (int)(dScore * 10));
        } catch (Exception e) {
            map.put("score", 0);
        }

        map.put("status", "watching");

        FirebaseFirestore.getInstance().collection("watchlist").document(uid)
                .collection("anime").document(String.valueOf(id)).set(map)
                .addOnSuccessListener(u -> {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Added to My List!", Toast.LENGTH_SHORT).show();
                    btnWatchlist.setText("Watching");
                    btnWatchlist.setEnabled(false);
                    btnWatchlist.setAlpha(0.8f);
                    btnWatchlist.setIconResource(R.drawable.ic_check);
                    NotificationHelper.sendNotification(getContext(), "Added to My List", title.getText().toString() + " is now in your list!");
                });
    }

    private void updateStatusToCompleted(int id) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.AnimeAlertDialog)
                .setTitle("Mark as Completed")
                .setMessage("Have you finished watching this anime?")
                .setPositiveButton("Yes, I'm done!", (dialog, which) -> {
                    performUpdateStatusToCompleted(id);
                })
                .setNegativeButton("Not yet", null)
                .show();
    }

    private void performUpdateStatusToCompleted(int id) {
        String uid = FirebaseUtils.uid();
        if (uid == null) return;

        FirebaseFirestore.getInstance().collection("watchlist").document(uid)
                .collection("anime").document(String.valueOf(id))
                .update("status", "completed")
                .addOnSuccessListener(aVoid -> {
                    btnComplete.setText("COMPLETED");
                    btnComplete.setEnabled(false);
                    btnComplete.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                    Toast.makeText(getContext(), "Marked as Completed!", Toast.LENGTH_SHORT).show();

                    FirebaseFirestore.getInstance().collection("users")
                            .document(uid)
                            .update("watchedCount", FieldValue.increment(1));

                    NotificationHelper.sendNotification(getContext(), "Anime Completed!", "You've finished watching " + title.getText().toString() + "!");
                });
    }

    private void removeFromWatchlist(int id) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.AnimeAlertDialog)
                .setTitle("Remove from Watchlist")
                .setMessage("Are you sure you want to remove this anime from your watchlist?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    performRemoveFromWatchlist(id);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performRemoveFromWatchlist(int id) {
        String uid = FirebaseUtils.uid();
        if (uid == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("watchlist").document(uid)
                .collection("anime").document(String.valueOf(id)).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        boolean wasCompleted = "completed".equals(doc.getString("status"));
                        
                        // Move to 'dropped' instead of deleting
                        db.collection("watchlist").document(uid)
                                .collection("anime").document(String.valueOf(id))
                                .update("status", "dropped")
                                .addOnSuccessListener(aVoid -> {
                                    if (wasCompleted) {
                                        db.collection("users").document(uid)
                                                .update("watchedCount", FieldValue.increment(-1));
                                    }
                                    Toast.makeText(getContext(), "Moved to Dropped", Toast.LENGTH_SHORT).show();
                                    getParentFragmentManager().popBackStack();
                                });
                    }
                });
    }
}