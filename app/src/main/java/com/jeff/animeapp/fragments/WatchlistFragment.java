package com.jeff.animeapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jeff.animeapp.adapters.AnimeAdapter;
import com.jeff.animeapp.databinding.FragmentWatchlistBinding; // Import the binding
import com.jeff.animeapp.utils.FirebaseUtils;
import com.google.firebase.firestore.DocumentSnapshot;

public class WatchlistFragment extends Fragment {

    // 1. Create the binding variable
    private FragmentWatchlistBinding binding;

    public WatchlistFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 2. Initialize the binding
        binding = FragmentWatchlistBinding.inflate(inflater, container, false);

        // 3. Access views via binding (No findViewById needed!)
        binding.recyclerWatchlist.setLayoutManager(new LinearLayoutManager(getContext()));

        loadWatchlist();

        return binding.getRoot();
    }

    private void loadWatchlist() {
        if (FirebaseUtils.uid() == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressWatchlist.setVisibility(View.VISIBLE);

        FirebaseUtils.firestore()
                .collection("watchlist")
                .document(FirebaseUtils.uid())
                .collection("anime")
                .get()
                .addOnSuccessListener(snapshot -> {
                    binding.progressWatchlist.setVisibility(View.GONE);

                    JsonArray arr = new JsonArray();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        JsonObject obj = new JsonObject();
                        JsonObject titleObj = new JsonObject();
                        titleObj.addProperty("romaji", doc.getString("title"));

                        JsonObject imgObj = new JsonObject();
                        imgObj.addProperty("large", doc.getString("coverImage"));

                        obj.add("title", titleObj);
                        obj.add("coverImage", imgObj);
                        obj.addProperty("description", doc.getString("description"));
                        obj.addProperty("averageScore", doc.getLong("score") != null ? doc.getLong("score") : 0);

                        arr.add(obj);
                    }

                    binding.recyclerWatchlist.setLayoutManager(new GridLayoutManager(getContext(), 2));
                    binding.recyclerWatchlist.setAdapter(new AnimeAdapter(arr));
                })
                .addOnFailureListener(e -> {
                    binding.progressWatchlist.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Important for memory management
    }
}