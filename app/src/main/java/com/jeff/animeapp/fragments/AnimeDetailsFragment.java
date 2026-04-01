package com.jeff.animeapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
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
    private Button btnPlay, btnWatchlist;
    private String imageUrl = ""; // ✅ store image URL

    public static AnimeDetailsFragment newInstance(int id) {
        AnimeDetailsFragment fragment = new AnimeDetailsFragment();
        Bundle args = new Bundle();
        args.putInt("anime_id", id);
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

        int animeId = getArguments().getInt("anime_id");
        fetchAnimeDetails(animeId);

        btnWatchlist.setOnClickListener(view -> addToWatchlist(animeId));

        return v;
    }

    private void fetchAnimeDetails(int id) {
        String query = "query ($id: Int) { Media(id: $id, type: ANIME) { id title { romaji } description coverImage { large } averageScore } }";

        JsonObject variables = new JsonObject();
        variables.addProperty("id", id);

        JsonObject body = new JsonObject();
        body.addProperty("query", query);
        body.add("variables", variables);

        AniListClient.API api = AniListClient.getClient().create(AniListClient.API.class);
        api.query(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject media = response.body()
                        .getAsJsonObject("data")
                        .getAsJsonObject("Media");

                String titleStr = media.getAsJsonObject("title").get("romaji").getAsString();
                String descStr = media.get("description").getAsString().replaceAll("<.*?>", "");
                imageUrl = media.getAsJsonObject("coverImage").get("large").getAsString(); // ✅ save image URL
                int scoreInt = media.get("averageScore").getAsInt();

                title.setText(titleStr);
                description.setText(descStr);
                score.setText("⭐ " + scoreInt);

                Glide.with(getContext()).load(imageUrl).into(cover);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToWatchlist(int id) {
        String uid = FirebaseUtils.uid();
        if (uid == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id); // ✅ Save anime ID
        map.put("title", title.getText().toString());
        map.put("coverImage", imageUrl); // ✅ store actual image URL
        map.put("description", description.getText().toString());

        // ✅ Save score as number
        String scoreText = score.getText().toString().replace("⭐", "").trim();
        try {
            int scoreInt = Integer.parseInt(scoreText);
            map.put("score", scoreInt);
        } catch (NumberFormatException e) {
            map.put("score", 0);
        }

        FirebaseFirestore.getInstance()
                .collection("watchlist")
                .document(uid)
                .collection("anime")
                .document(String.valueOf(id)) // ✅ Use ID as doc name
                .set(map)
                .addOnSuccessListener(unused ->
                        Toast.makeText(getContext(), "Added to Watchlist!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}