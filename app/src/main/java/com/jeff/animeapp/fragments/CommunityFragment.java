package com.jeff.animeapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

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

    public CommunityFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_community, container, false);

        recyclerView = v.findViewById(R.id.recyclerCommunity);
        progressBar = v.findViewById(R.id.progressCommunity);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchPosts();

        return v;
    }

    private void fetchPosts() {
        progressBar.setVisibility(View.VISIBLE);

        KitsuClient.API api = KitsuClient.getClient().create(KitsuClient.API.class);
        api.getCommunityPosts().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressBar.setVisibility(View.GONE);

                if (response.body() == null) return;

                // Now the adapter matches item_post.xml
                recyclerView.setAdapter(new PostAdapter(response.body()));
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to fetch posts", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }
}