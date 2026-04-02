package com.jeff.animeapp.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.jeff.animeapp.R;
import com.jeff.animeapp.api.AniListClient;
import com.jeff.animeapp.utils.FirebaseUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnimeDetailsFragment extends Fragment {

    private ImageView cover;
    private TextView title, score, description;
    private Button btnPlay, btnWatchlist, btnComplete, btnRemove; // Added buttons
    private View layoutWatchlistActions; // Container ng buttons
    private String imageUrl = "";
    private boolean isFromWatchlist = false; // Flag para sa UI logic

    // Updated newInstance para tumanggap ng isWatchlist boolean
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
        title = v.findViewById(R.id.animeTitle);
        score = v.findViewById(R.id.animeScore);
        description = v.findViewById(R.id.animeDescription);
        btnPlay = v.findViewById(R.id.btnPlay);
        btnWatchlist = v.findViewById(R.id.btnWatchlist);

        // Watchlist Action Buttons (Galing sa layout)
        btnComplete = v.findViewById(R.id.btnDetailsComplete);
        btnRemove = v.findViewById(R.id.btnDetailsRemove);
        layoutWatchlistActions = v.findViewById(R.id.layoutDetailsActions);

        if (getArguments() != null) {
            int animeId = getArguments().getInt("anime_id");
            isFromWatchlist = getArguments().getBoolean("is_watchlist");

            fetchAnimeDetails(animeId);

            // LOGIC PARA SA VISIBILITY NG BUTTONS
            if (isFromWatchlist) {
                btnWatchlist.setVisibility(View.GONE); // Itago ang "Add"
                layoutWatchlistActions.setVisibility(View.VISIBLE); // Ipakita ang "Done/Remove"

                checkStatus(animeId); // I-check kung completed na ba
            } else {
                btnWatchlist.setVisibility(View.VISIBLE);
                layoutWatchlistActions.setVisibility(View.GONE);
            }

            // BUTTON LISTENERS
            btnWatchlist.setOnClickListener(view -> addToWatchlist(animeId));

            btnRemove.setOnClickListener(view -> removeFromWatchlist(animeId));

            btnComplete.setOnClickListener(view -> updateStatusToCompleted(animeId));
        }

        return v;
    }

    private void fetchAnimeDetails(int id) {
        String query = "query ($id: Int) { Media(id: $id, type: ANIME) { id title { romaji } description coverImage { large } averageScore } }";
        JsonObject variables = new JsonObject();
        variables.addProperty("id", id);
        JsonObject body = new JsonObject();
        body.addProperty("query", query);
        body.add("variables", variables);

        AniListClient.getClient().create(AniListClient.API.class).query(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.body() != null) {
                    JsonObject media = response.body().getAsJsonObject("data").getAsJsonObject("Media");
                    String titleStr = media.getAsJsonObject("title").get("romaji").getAsString();
                    String descStr = media.get("description").getAsString().replaceAll("<.*?>", "");
                    imageUrl = media.getAsJsonObject("coverImage").get("large").getAsString();
                    int scoreInt = media.get("averageScore").getAsInt();

                    title.setText(titleStr);
                    description.setText(descStr);
                    score.setText("⭐ " + scoreInt);
                    Glide.with(getContext()).load(imageUrl).into(cover);
                }
            }
            @Override public void onFailure(Call<JsonObject> call, Throwable t) {}
        });
    }

    private void checkStatus(int id) {
        String uid = FirebaseUtils.uid();
        if (uid == null) return;

        FirebaseFirestore.getInstance().collection("watchlist").document(uid)
                .collection("anime").document(String.valueOf(id)).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && "completed".equals(doc.getString("status"))) {
                        btnComplete.setText("COMPLETED");
                        btnComplete.setEnabled(false);
                        btnComplete.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
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
        map.put("status", "watching");

        FirebaseFirestore.getInstance().collection("watchlist").document(uid)
                .collection("anime").document(String.valueOf(id)).set(map)
                .addOnSuccessListener(u -> Toast.makeText(getContext(), "Added!", Toast.LENGTH_SHORT).show());
    }

    private void updateStatusToCompleted(int id) {
        String uid = FirebaseUtils.uid();
        FirebaseFirestore.getInstance().collection("watchlist").document(uid)
                .collection("anime").document(String.valueOf(id))
                .update("status", "completed")
                .addOnSuccessListener(aVoid -> {
                    btnComplete.setText("COMPLETED");
                    btnComplete.setEnabled(false);
                    btnComplete.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                    Toast.makeText(getContext(), "Marked as Completed!", Toast.LENGTH_SHORT).show();
                });
    }

    private void removeFromWatchlist(int id) {
        String uid = FirebaseUtils.uid();
        FirebaseFirestore.getInstance().collection("watchlist").document(uid)
                .collection("anime").document(String.valueOf(id)).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Removed!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack(); // Bumalik sa listahan
                });
    }
}