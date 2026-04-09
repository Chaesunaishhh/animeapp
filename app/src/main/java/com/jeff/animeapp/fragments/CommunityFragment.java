package com.jeff.animeapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.jeff.animeapp.R;
import com.jeff.animeapp.adapters.PostAdapter;
import com.jeff.animeapp.api.KitsuClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    // Example anime ID (Demon Slayer). Replace with dynamic ID if needed.
    private static final int ANIME_ID = 1;

    public CommunityFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_community, container, false);

        recyclerView = v.findViewById(R.id.recyclerCommunity);
        progressBar = v.findViewById(R.id.progressCommunity);
        Button btnAddReview = v.findViewById(R.id.btnAddReview);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Fetch reviews when fragment loads
        fetchReviews(ANIME_ID);

        // Handle Add Review button
        btnAddReview.setOnClickListener(view -> showAddReviewDialog());

        return v;
    }

    private void fetchReviews(int animeId) {
        progressBar.setVisibility(View.VISIBLE);

        KitsuClient.API api = KitsuClient.getClient().create(KitsuClient.API.class);
        api.getAnimeReviews(animeId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE);

                if (response.body() == null) {
                    Toast.makeText(getContext(), "No reviews found", Toast.LENGTH_SHORT).show();
                    return;
                }

                recyclerView.setAdapter(new PostAdapter(response.body()));
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to fetch reviews", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    private void showAddReviewDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_review, null);

        EditText editAnimeTitle = dialogView.findViewById(R.id.editAnimeTitle);
        EditText editReview = dialogView.findViewById(R.id.editReview);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Add Review")
                .setView(dialogView)
                .setCancelable(false)
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

            submitReview(animeTitle, reviewText, rating, ANIME_ID);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void submitReview(String animeTitle, String reviewText, float rating, int animeId) {
        progressBar.setVisibility(View.VISIBLE);

        KitsuClient.API api = KitsuClient.getClient().create(KitsuClient.API.class);

        // Build JSON body according to Kitsu schema
        JsonObject body = new JsonObject();
        JsonObject data = new JsonObject();
        JsonObject attributes = new JsonObject();
        JsonObject relationships = new JsonObject();
        JsonObject anime = new JsonObject();
        JsonObject animeData = new JsonObject();

        attributes.addProperty("content", reviewText);
        attributes.addProperty("rating", rating);
        attributes.addProperty("animeTitle", animeTitle);

        animeData.addProperty("type", "anime");
        animeData.addProperty("id", animeId);
        anime.add("data", animeData);
        relationships.add("anime", anime);

        data.addProperty("type", "reviews");
        data.add("attributes", attributes);
        data.add("relationships", relationships);

        body.add("data", data);

        api.addReview(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Review submitted!", Toast.LENGTH_SHORT).show();
                    fetchReviews(animeId); // refresh reviews
                } else {
                    Toast.makeText(getContext(), "Failed to submit review (auth required)", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error submitting review", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }
}
