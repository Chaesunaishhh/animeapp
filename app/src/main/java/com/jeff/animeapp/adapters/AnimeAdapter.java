package com.jeff.animeapp.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jeff.animeapp.R;
import com.jeff.animeapp.utils.FirebaseUtils;
import java.util.HashMap;

public class AnimeAdapter extends RecyclerView.Adapter<AnimeAdapter.ViewHolder> {

    private final JsonArray animeList;
    private final OnAnimeClickListener listener;
    private final boolean isWatchlist;

    public interface OnAnimeClickListener {
        void onAnimeClick(int id);
    }

    public AnimeAdapter(JsonArray animeList, boolean isWatchlist, OnAnimeClickListener listener) {
        this.animeList = animeList;
        this.isWatchlist = isWatchlist;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_anime, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject anime = animeList.get(position).getAsJsonObject();

        // Data Parsing
        String titleStr = anime.has("title") && anime.getAsJsonObject("title").has("romaji")
                ? anime.getAsJsonObject("title").get("romaji").getAsString() : "Unknown Title";

        String imageStr = anime.has("coverImage") && anime.getAsJsonObject("coverImage").has("large")
                ? anime.getAsJsonObject("coverImage").get("large").getAsString() : "";

        String descStr = anime.has("description") && !anime.get("description").isJsonNull()
                ? anime.get("description").getAsString().replaceAll("<.*?>", "") : "No description.";

        int scoreInt = anime.has("averageScore") && !anime.get("averageScore").isJsonNull()
                ? anime.get("averageScore").getAsInt() : 0;

        final int finalId = anime.has("id") ? anime.get("id").getAsInt() : 0;

        // Set Basic UI
        holder.title.setText(titleStr);
        holder.score.setText("⭐ " + scoreInt);
        holder.desc.setText(descStr);
        Glide.with(holder.itemView.getContext()).load(imageStr).into(holder.image);

        // --- BUTTON LOGIC START ---
        if (isWatchlist) {
            holder.btnWishlist.setVisibility(View.GONE);
            holder.layoutWatchlistActions.setVisibility(View.VISIBLE);

            String status = anime.has("status") ? anime.get("status").getAsString() : "watching";

            if ("completed".equalsIgnoreCase(status)) {
                // 1. Hide the Remove button
                holder.btnRemove.setVisibility(View.GONE);

                // 2. Change text and disable
                holder.btnComplete.setText("COMPLETED");
                holder.btnComplete.setEnabled(false);
                holder.btnComplete.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));

                // 3. Make button span full width (Weight 2.0)
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2.0f);
                holder.btnComplete.setLayoutParams(params);
            } else {
                // Default State (Watching)
                holder.btnRemove.setVisibility(View.VISIBLE);
                holder.btnComplete.setText("DONE");
                holder.btnComplete.setEnabled(true);
                holder.btnComplete.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));

                // Reset weights to 1:1
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                holder.btnComplete.setLayoutParams(params);
                holder.btnRemove.setLayoutParams(params);
            }
        } else {
            holder.btnWishlist.setVisibility(View.VISIBLE);
            holder.layoutWatchlistActions.setVisibility(View.GONE);
        }
        // --- BUTTON LOGIC END ---

        // Click Listeners
        holder.btnWishlist.setOnClickListener(v -> addToWatchlist(v, anime));

        holder.btnComplete.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                updateStatusToCompleted(v, finalId, currentPos);
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                removeFromWatchlist(v, finalId, currentPos);
            }
        });

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
        map.put("description", anime.has("description") ? anime.get("description").getAsString() : "");
        map.put("score", anime.has("averageScore") ? anime.get("averageScore").getAsInt() : 0);
        map.put("status", "watching");

        FirebaseUtils.firestore().collection("watchlist").document(uid)
                .collection("anime").document(String.valueOf(id)).set(map)
                .addOnSuccessListener(u -> {
                    Toast.makeText(view.getContext(), "Saved to Watchlist!", Toast.LENGTH_SHORT).show();
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
            if (obj.has("id") && obj.get("id").getAsInt() == id) return i;
        }
        return -1;
    }

    private void updateStatusToCompleted(View view, int id, int position) {
        String uid = FirebaseUtils.uid();
        if (uid == null) return;

        FirebaseUtils.firestore().collection("watchlist").document(uid)
                .collection("anime").document(String.valueOf(id))
                .update("status", "completed")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(view.getContext(), "Status updated to Completed!", Toast.LENGTH_SHORT).show();
                    JsonObject anime = animeList.get(position).getAsJsonObject();
                    anime.addProperty("status", "completed");
                    notifyItemChanged(position); // Re-runs onBindViewHolder to fix the layout
                });
    }

    private void removeFromWatchlist(View view, int id, int position) {
        String uid = FirebaseUtils.uid();
        FirebaseUtils.firestore().collection("watchlist").document(uid)
                .collection("anime").document(String.valueOf(id)).delete()
                .addOnSuccessListener(aVoid -> {
                    animeList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, animeList.size());
                    Toast.makeText(view.getContext(), "Removed!", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() { return animeList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, score, desc;
        Button btnWishlist, btnComplete, btnRemove;
        LinearLayout layoutWatchlistActions; // Use LinearLayout specifically for weight handling

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.animeImage);
            title = itemView.findViewById(R.id.animeTitle);
            score = itemView.findViewById(R.id.animeScore);
            desc = itemView.findViewById(R.id.animeDesc);
            btnWishlist = itemView.findViewById(R.id.btnWishlist);
            btnComplete = itemView.findViewById(R.id.btnComplete);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            layoutWatchlistActions = itemView.findViewById(R.id.layoutWatchlistActions);
        }
    }
}