package com.jeff.animeapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jeff.animeapp.R;
import com.jeff.animeapp.adapters.WatchlistAdapter;
import com.jeff.animeapp.databinding.FragmentWatchlistBinding;
import com.jeff.animeapp.utils.FirebaseUtils;
import com.google.firebase.firestore.DocumentSnapshot;

public class WatchlistFragment extends Fragment {

    private FragmentWatchlistBinding binding;

    public WatchlistFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWatchlistBinding.inflate(inflater, container, false);

        // Use LinearLayoutManager for vertical list (one item per row)
        binding.recyclerWatchlist.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)
        );

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
                    if (!isAdded() || binding == null) return;

                    binding.progressWatchlist.setVisibility(View.GONE);

                    JsonArray arr = new JsonArray();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        JsonObject obj = new JsonObject();

                        long id = doc.contains("id") && doc.getLong("id") != null ? doc.getLong("id") : 0;
                        obj.addProperty("id", id);

                        JsonObject titleObj = new JsonObject();
                        titleObj.addProperty("romaji", doc.getString("title") != null ? doc.getString("title") : "Unknown");
                        obj.add("title", titleObj);

                        JsonObject imgObj = new JsonObject();
                        imgObj.addProperty("large", doc.getString("coverImage") != null ? doc.getString("coverImage") : "");
                        obj.add("coverImage", imgObj);

                        obj.addProperty("averageScore", doc.getLong("score") != null ? doc.getLong("score") : 0);
                        obj.addProperty("status", doc.getString("status") != null ? doc.getString("status") : "watching");

                        arr.add(obj);
                    }

                    // Update watchlist count text
                    int count = snapshot.size();
                    binding.watchlistCount.setText(count + " anime in your list");

                    // Update Firestore field for watchlistCount
                    FirebaseUtils.firestore()
                            .collection("users")
                            .document(FirebaseUtils.uid())
                            .update("watchlistCount", count);

                    // Use WatchlistAdapter
                    WatchlistAdapter adapter = new WatchlistAdapter(arr, id -> {
                        Fragment detailsFragment = AnimeDetailsFragment.newInstance((int) id, true);
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainer, detailsFragment)
                                .addToBackStack(null)
                                .commit();
                    });

                    binding.recyclerWatchlist.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    binding.progressWatchlist.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
