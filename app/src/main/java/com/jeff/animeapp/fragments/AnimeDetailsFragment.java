package com.jeff.animeapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.jeff.animeapp.R;
import com.jeff.animeapp.api.AniListClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnimeDetailsFragment extends Fragment {

    private static final String ARG_ANIME_ID = "anime_id";

    private int animeId;
    private ImageView animePoster;
    private TextView animeTitle, animeDescription, animeScore;

    public AnimeDetailsFragment() {}

    public static AnimeDetailsFragment newInstance(int animeId) {
        AnimeDetailsFragment fragment = new AnimeDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ANIME_ID, animeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            animeId = getArguments().getInt(ARG_ANIME_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_anime_details, container, false);

        // Bind XML views
        animePoster = v.findViewById(R.id.animeCover);
        animeTitle = v.findViewById(R.id.animeTitle);
        animeDescription = v.findViewById(R.id.animeDescription);
        animeScore = v.findViewById(R.id.statDefense); // <-- you can repurpose a TextView or add a new one for score in XML

        loadDetails();

        return v;
    }

    private void loadDetails() {
        // Classic string concatenation (works in all Java versions)
        String query = "query {"
                + " Media(id: " + animeId + ", type: ANIME) {"
                + " title { romaji }"
                + " description"
                + " averageScore"
                + " coverImage { large }"
                + " }"
                + "}";

        JsonObject body = new JsonObject();
        body.addProperty("query", query);

        AniListClient.API api = AniListClient.getClient().create(AniListClient.API.class);

        api.query(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.body() == null) return;

                JsonObject media = response.body()
                        .getAsJsonObject("data")
                        .getAsJsonObject("Media");

                // Set data to views
                animeTitle.setText(
                        media.getAsJsonObject("title")
                                .get("romaji").getAsString()
                );

                animeDescription.setText(
                        media.get("description").getAsString()
                                .replace("<br>", "")
                                .replace("<i>", "").replace("</i>", "")
                );

                animeScore.setText("Score: " + media.get("averageScore").getAsInt());

                Glide.with(getContext())
                        .load(media.getAsJsonObject("coverImage").get("large").getAsString())
                        .into(animePoster);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load anime details", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }
}