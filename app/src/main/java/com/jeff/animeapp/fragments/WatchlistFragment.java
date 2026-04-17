package com.jeff.animeapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jeff.animeapp.R;
import com.jeff.animeapp.adapters.WatchlistAdapter;
import com.jeff.animeapp.databinding.FragmentWatchlistBinding;
import com.jeff.animeapp.utils.FirebaseUtils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

public class WatchlistFragment extends Fragment {

    private FragmentWatchlistBinding binding;
    private ListenerRegistration watchlistListener;

    public WatchlistFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWatchlistBinding.inflate(inflater, container, false);

        setupUI();
        startWatchlistListener();

        return binding.getRoot();
    }

    private void setupUI() {
        binding.recyclerWatching.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerCompleted.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerPlanning.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.watchlistTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateTabVisibility(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void updateTabVisibility(int position) {
        binding.recyclerWatching.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        binding.recyclerCompleted.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
        binding.recyclerPlanning.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
    }

    private void startWatchlistListener() {
        if (FirebaseUtils.uid() == null) return;

        binding.progressWatchlist.setVisibility(View.VISIBLE);
        
        // Use SnapshotListener for REAL-TIME updates
        watchlistListener = FirebaseUtils.firestore()
                .collection("watchlist")
                .document(FirebaseUtils.uid())
                .collection("anime")
                .addSnapshotListener((snapshot, e) -> {
                    if (!isAdded() || binding == null || snapshot == null) return;
                    
                    binding.progressWatchlist.setVisibility(View.GONE);

                    JsonArray watchingList = new JsonArray();
                    JsonArray completedList = new JsonArray();
                    JsonArray planningList = new JsonArray();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        JsonObject obj = new JsonObject();
                        obj.addProperty("id", doc.getLong("id"));
                        
                        JsonObject titleObj = new JsonObject();
                        titleObj.addProperty("romaji", doc.getString("title"));
                        obj.add("title", titleObj);

                        JsonObject imgObj = new JsonObject();
                        imgObj.addProperty("large", doc.getString("coverImage"));
                        obj.add("coverImage", imgObj);

                        obj.addProperty("averageScore", doc.getLong("score"));
                        String status = doc.getString("status") != null ? doc.getString("status").toLowerCase() : "watching";
                        obj.addProperty("status", status);

                        // Proper sorting logic
                        if (status.equals("completed")) completedList.add(obj);
                        else if (status.equals("planning")) planningList.add(obj);
                        else watchingList.add(obj);
                        // 'dropped' is no longer supported and will be ignored
                    }

                    // Update Adapters
                    binding.recyclerWatching.setAdapter(new WatchlistAdapter(watchingList, id -> openDetails(id)));
                    binding.recyclerCompleted.setAdapter(new WatchlistAdapter(completedList, id -> openDetails(id)));
                    binding.recyclerPlanning.setAdapter(new WatchlistAdapter(planningList, id -> openDetails(id)));
                    
                    // Show empty states if list is empty
                    boolean isEmpty = snapshot.isEmpty();
                    // You could add an empty view here if you want
                });
    }

    private void openDetails(long id) {
        Fragment detailsFragment = AnimeDetailsFragment.newInstance((int) id, true);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, detailsFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (watchlistListener != null) watchlistListener.remove();
        binding = null;
    }
}
