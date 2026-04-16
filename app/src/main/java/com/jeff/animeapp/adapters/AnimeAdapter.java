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
    private boolean isGrid = false;

    public interface OnAnimeClickListener {
        void onAnimeClick(int id);
    }

    public AnimeAdapter(JsonArray animeList, OnAnimeClickListener listener) {
        this.animeList = animeList;
        this.listener = listener;
    }

    private boolean isWatchlistHorizontal = false;

    public AnimeAdapter(JsonArray animeList, boolean isGrid, OnAnimeClickListener listener) {
        this.animeList = animeList;
        this.isGrid = isGrid;
        this.listener = listener;
    }

    public AnimeAdapter(JsonArray animeList, boolean isGrid, boolean isWatchlistHorizontal, OnAnimeClickListener listener) {
        this.animeList = animeList;
        this.isGrid = isGrid;
        this.isWatchlistHorizontal = isWatchlistHorizontal;
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
        int layoutId = isWatchlistHorizontal ? R.layout.item_watchlist_horizontal : R.layout.item_anime;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false);

        if (isGrid && !isWatchlistHorizontal) {
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (lp != null) {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                view.setLayoutParams(lp);
            }
        }

        return new ViewHolder(view);
    }

    private java.util.Set<Integer> watchlistIds = new java.util.HashSet<>();

    public void setWatchlistIds(java.util.Set<Integer> ids) {
        this.watchlistIds = ids;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject anime = animeList.get(position).getAsJsonObject();

        // Data parsing
        String titleStr = "Unknown Title";
        if (anime.has("title")) {
            if (anime.get("title").isJsonObject()) {
                titleStr = anime.getAsJsonObject("title").has("romaji")
                        ? anime.getAsJsonObject("title").get("romaji").getAsString()
                        : "Unknown Title";
            } else {
                titleStr = anime.get("title").getAsString();
            }
        }

        String imageStr = "";
        if (anime.has("coverImage")) {
            if (anime.get("coverImage").isJsonObject()) {
                imageStr = anime.getAsJsonObject("coverImage").has("large")
                        ? anime.getAsJsonObject("coverImage").get("large").getAsString()
                        : "";
            } else {
                imageStr = anime.get("coverImage").getAsString();
            }
        }

        int scoreInt = anime.has("averageScore") && !anime.get("averageScore").isJsonNull()
                ? anime.get("averageScore").getAsInt()
                : 0;
        
        // Handle both API (averageScore) and Firestore (score)
        if (scoreInt == 0 && anime.has("score") && !anime.get("score").isJsonNull()) {
            scoreInt = anime.get("score").getAsInt();
        }

        double scoreFormatted = scoreInt / 10.0;

        final int finalId = anime.has("id") ? anime.get("id").getAsInt() : 0;

        // Bind UI
        if (holder.title != null) holder.title.setText(titleStr);
        if (holder.score != null) {
            if (isWatchlistHorizontal) {
                holder.score.setText(scoreInt + "%");
            } else {
                holder.score.setText("⭐ " + scoreFormatted);
            }
        }
        Glide.with(holder.itemView.getContext())
                .load(imageStr)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(holder.image);

        // Badge visibility
        View badge = holder.itemView.findViewById(R.id.badgeInWatchlist);
        if (badge != null) {
            badge.setVisibility(watchlistIds.contains(finalId) ? View.VISIBLE : View.GONE);
        }

        // Add button logic - change icon if already in list
        if (holder.btnWishlist != null) {
            if (watchlistIds.contains(finalId)) {
                holder.btnWishlist.setImageResource(R.drawable.ic_check); // Assuming ic_check exists
                holder.btnWishlist.setEnabled(false);
                holder.btnWishlist.setAlpha(0.5f);
            } else {
                holder.btnWishlist.setImageResource(R.drawable.ic_add);
                holder.btnWishlist.setEnabled(true);
                holder.btnWishlist.setAlpha(1.0f);
                holder.btnWishlist.setOnClickListener(v -> addToWatchlist(v, anime));
            }
        }

        // Pag click ng item, mag oopen yung details
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
                    Toast.makeText(view.getContext(), "Added to Watchlist!", Toast.LENGTH_SHORT).show();
                    
                    // Update local state and refresh
                    watchlistIds.add(id);
                    notifyDataSetChanged();
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView title, score;
        public ImageButton btnWishlist;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.animeImage);
            title = itemView.findViewById(R.id.animeTitle);
            score = itemView.findViewById(R.id.animeScore);
            btnWishlist = itemView.findViewById(R.id.btnWishlist);
        }
    }
}
