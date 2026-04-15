package com.jeff.animeapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
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
import com.jeff.animeapp.adapters.CharacterAdapter;  // ← CHANGED: was PostAdapter
import com.jeff.animeapp.adapters.PostAdapter;

import java.util.HashMap;
import java.util.Map;

public class CommunityFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private String currentUser;
    private PostAdapter postAdapter;

    // Tab UI Elements
    private LinearLayout layoutReviews, layoutCharacters;
    private TextView tabReviews, tabCharacters;
    private RecyclerView recyclerCharacters;
    private CharacterAdapter characterAdapter;  // ← CHANGED: was PostAdapter

    private static final int ANIME_ID = 1;

    public CommunityFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_community, container, false);

        // 1. Initialize View IDs
        recyclerView = v.findViewById(R.id.recyclerCommunity);
        progressBar = v.findViewById(R.id.progressCommunity);
        Button btnAddReview = v.findViewById(R.id.btnAddReview);

        layoutReviews = v.findViewById(R.id.layoutReviews);
        layoutCharacters = v.findViewById(R.id.layoutCharacters);
        tabReviews = v.findViewById(R.id.tabReviews);
        tabCharacters = v.findViewById(R.id.tabCharacters);
        recyclerCharacters = v.findViewById(R.id.recyclerCharacters);

        // 2. Set Layout Managers
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerCharacters.setLayoutManager(new LinearLayoutManager(getContext()));

        // 3. Session Management
        SharedPreferences userSession = requireActivity()
                .getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUser = userSession.getString("logged_in_user", "Guest");

        // 4. Initial Load (Reviews)
        fetchReviews(ANIME_ID);

        // 5. Click Listeners
        btnAddReview.setOnClickListener(view -> showAddReviewDialog());

        tabReviews.setOnClickListener(view -> switchTab(true));
        tabCharacters.setOnClickListener(view -> switchTab(false));

        return v;
    }

    private void switchTab(boolean isReviews) {
        if (isReviews) {
            layoutReviews.setVisibility(View.VISIBLE);
            layoutCharacters.setVisibility(View.GONE);

            tabReviews.setBackgroundResource(R.drawable.tab_selected_bg);
            tabCharacters.setBackground(null);

            fetchReviews(ANIME_ID);
        } else {
            layoutReviews.setVisibility(View.GONE);
            layoutCharacters.setVisibility(View.VISIBLE);

            tabCharacters.setBackgroundResource(R.drawable.tab_selected_bg);
            tabReviews.setBackground(null);

            fetchCharacters();
        }
    }

    private void fetchReviews(int animeId) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reviews")
                .whereEqualTo("animeId", String.valueOf(animeId))
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    progressBar.setVisibility(View.GONE);
                    if (snapshots != null) {
                        postAdapter = new PostAdapter(snapshots.getDocuments());
                        recyclerView.setAdapter(postAdapter);
                    }
                });
    }

    // ← THIS IS THE ONLY METHOD THAT CHANGED
    private void fetchCharacters() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("characters")
                .orderBy("votes", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (snapshots != null) {
                        // Uses CharacterAdapter instead of PostAdapter
                        // Passes currentUser so the adapter knows who is logged in
                        characterAdapter = new CharacterAdapter(snapshots.getDocuments(), currentUser);
                        recyclerCharacters.setAdapter(characterAdapter);
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
        review.put("username", currentUser);
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