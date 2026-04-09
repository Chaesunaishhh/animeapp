package com.jeff.animeapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jeff.animeapp.R;
import com.jeff.animeapp.utils.FirebaseUtils;

import java.util.HashMap;

public class AnimeAdapter extends RecyclerView.Adapter<AnimeAdapter.ViewHolder> {

    private JsonArray animeList;
    private final OnAnimeClickListener listener;

    public interface OnAnimeClickListener {
        void onAnimeClick(int id);
    }

    public AnimeAdapter(JsonArray animeList, OnAnimeClickListener listener) {
        this.animeList = animeList;
        this.listener = listener;
    }

    // Getter for HomeFragment filtering
    public JsonArray getMediaArray() {
        return animeList;
    }

    // Update adapter data after filtering
    public void updateData(JsonArray newList) {
        animeList = newList.deepCopy();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_anime, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject anime = animeList.get(position).getAsJsonObject();

        // Data parsing
        String titleStr = anime.has("title") && anime.getAsJsonObject("title").has("romaji")
                ? anime.getAsJsonObject("title").get("romaji").getAsString()
                : "Unknown Title";

        String imageStr = anime.has("coverImage") && anime.getAsJsonObject("coverImage").has("large")
                ? anime.getAsJsonObject("coverImage").get("large").getAsString()
                : "";

        int scoreInt = anime.has("averageScore") && !anime.get("averageScore").isJsonNull()
                ? anime.get("averageScore").getAsInt()
                : 0;

        final int finalId = anime.has("id") ? anime.get("id").getAsInt() : 0;

        // Bind UI
        holder.title.setText(titleStr);
        holder.score.setText("⭐ " + scoreInt);
        Glide.with(holder.itemView.getContext()).load(imageStr).into(holder.image);

        // Add button logic
        holder.btnWishlist.setOnClickListener(v -> addToWatchlist(v, anime));

        // Item click → open details
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAnimeClick(finalId);
        });
    }

    private void addToWatchlist(View view, JsonObject anime) {
        String uid = FirebaseUtils.uid();
        if (uid == null) {
            Toast.makeText(view.getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        int id = anime.get("id").getAsInt();
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("title", anime.getAsJsonObject("title").get("romaji").getAsString());
        map.put("coverImage", anime.getAsJsonObject("coverImage").get("large").getAsString());
        map.put("score", anime.has("averageScore") ? anime.get("averageScore").getAsInt() : 0);
        map.put("status", "watching");

        FirebaseUtils.firestore().collection("watchlist").document(uid)
                .collection("anime").document(String.valueOf(id)).set(map)
                .addOnSuccessListener(u -> {
                    Toast.makeText(view.getContext(), "Saved to Watchlist!", Toast.LENGTH_SHORT).show();

                    // Remove from Home list
                    int position = getPositionById(id);
                    if (position != -1) {
                        animeList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, animeList.size());
                    }
                });
    }

    private int getPositionById(int id) {
        for (int i = 0; i < animeList.size(); i++) {
            JsonObject obj = animeList.get(i).getAsJsonObject();
            if (obj.has("id") && obj.get("id").getAsInt() == id) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return animeList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, score;
        ImageButton btnWishlist;

        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.animeImage);
            title = itemView.findViewById(R.id.animeTitle);
            score = itemView.findViewById(R.id.animeScore);
            btnWishlist = itemView.findViewById(R.id.btnWishlist);
        }
    }
}
