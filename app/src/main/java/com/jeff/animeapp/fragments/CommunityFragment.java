package com.jeff.animeapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.jeff.animeapp.R;
import com.jeff.animeapp.adapters.PostAdapter;

import java.util.HashMap;
import java.util.Map;

public class CommunityFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private String currentUser;
    private PostAdapter postAdapter;

    // Example anime ID (replace with dynamic ID if needed)
    private static final int ANIME_ID = 1;

    public CommunityFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_community, container, false);

        recyclerView = v.findViewById(R.id.recyclerCommunity);
        progressBar = v.findViewById(R.id.progressCommunity);
        Button btnAddReview = v.findViewById(R.id.btnAddReview);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // ✅ Get logged-in user from SharedPreferences
        SharedPreferences userSession = requireActivity()
                .getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUser = userSession.getString("logged_in_user", "Guest");

        // ✅ Fetch reviews in real-time
        fetchReviews(ANIME_ID);

        // Handle Add Review button
        btnAddReview.setOnClickListener(view -> showAddReviewDialog());

        return v;
    }

    private void fetchReviews(int animeId) {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reviews")
                .whereEqualTo("animeId", String.valueOf(animeId))
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    progressBar.setVisibility(View.GONE);
                    if (e != null) {
                        Toast.makeText(getContext(), "Error loading reviews", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshots != null) {
                        postAdapter = new PostAdapter(snapshots.getDocuments());
                        recyclerView.setAdapter(postAdapter);
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

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> review = new HashMap<>();
        review.put("animeId", String.valueOf(animeId));
        review.put("animeTitle", animeTitle);
        review.put("reviewText", reviewText);
        review.put("rating", rating);
        review.put("username", currentUser); // ✅ actual logged-in user
        review.put("timestamp", System.currentTimeMillis());

        db.collection("reviews")
                .add(review)
                .addOnSuccessListener(docRef -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Review submitted!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
