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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.firebase.firestore.FieldValue;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jeff.animeapp.R;
import com.jeff.animeapp.utils.FirebaseUtils;

import java.util.HashMap;
import java.util.Map;

public class WatchlistAdapter extends RecyclerView.Adapter<WatchlistAdapter.ViewHolder> {

    private final JsonArray animeList;
    private final AnimeAdapter.OnAnimeClickListener listener;

    public WatchlistAdapter(JsonArray animeList, AnimeAdapter.OnAnimeClickListener listener) {
        this.animeList = animeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_watchlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject anime = animeList.get(position).getAsJsonObject();

        final String title = anime.has("title") ?
                (anime.get("title").isJsonObject() ? anime.getAsJsonObject("title").get("romaji").getAsString() : anime.get("title").getAsString()) :
                "Unknown";

        final String image = anime.has("coverImage") ?
                (anime.get("coverImage").isJsonObject() ? anime.getAsJsonObject("coverImage").get("large").getAsString() : anime.get("coverImage").getAsString()) :
                "";

        int score = anime.has("averageScore") ? anime.get("averageScore").getAsInt() : 0;
        int id = anime.has("id") ? anime.get("id").getAsInt() : 0;
        String status = anime.has("status") ? anime.get("status").getAsString().toLowerCase() : "watching";

        holder.title.setText(title);
        holder.score.setText("⭐ " + (score / 10.0));
        holder.status.setText(status.toUpperCase());

        Glide.with(holder.itemView.getContext())
                .load(image)
                .placeholder(R.drawable.placeholder_image)
                .transform(new RoundedCorners(24))
                .into(holder.image);

        // Status Backgrounds
        int bgRes;
        switch (status) {
            case "completed": bgRes = R.drawable.status_bg_completed; break;
            case "planning": bgRes = R.drawable.status_bg_planning; break;
            default: bgRes = R.drawable.status_bg_watching; break;
        }
        holder.status.setBackgroundResource(bgRes);

        // Update Button Logic
        holder.btnUpdate.setOnClickListener(v -> {
            String[] options = {"Watching", "Completed", "Planning"};
            new androidx.appcompat.app.AlertDialog.Builder(v.getContext(), R.style.AnimeAlertDialog)
                    .setTitle("Move to...")
                    .setItems(options, (dialog, which) -> {
                        String newStatus = options[which].toLowerCase();
                        updateAnimeStatus(v, id, status, newStatus);
                    })
                    .show();
        });

        // Delete Button Logic
        holder.btnDelete.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(v.getContext(), R.style.AnimeAlertDialog)
                    .setTitle("Remove Anime")
                    .setMessage("Remove " + title + " from your list?")
                    .setPositiveButton("Remove", (dialog, which) -> deleteAnime(v, id, status))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAnimeClick(id);
        });
    }

    private void updateAnimeStatus(View v, int id, String oldStatus, String newStatus) {
        if (oldStatus.equals(newStatus)) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);

        FirebaseUtils.firestore().collection("watchlist")
                .document(FirebaseUtils.uid())
                .collection("anime").document(String.valueOf(id))
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Update global stats
                    if (newStatus.equals("completed")) {
                        FirebaseUtils.firestore().collection("users")
                                .document(FirebaseUtils.uid())
                                .update("watchedCount", FieldValue.increment(1));
                    } else if (oldStatus.equals("completed")) {
                        FirebaseUtils.firestore().collection("users")
                                .document(FirebaseUtils.uid())
                                .update("watchedCount", FieldValue.increment(-1));
                    }
                    
                    Toast.makeText(v.getContext(), "Moved to " + newStatus, Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteAnime(View v, int id, String currentStatus) {
        FirebaseUtils.firestore().collection("watchlist")
                .document(FirebaseUtils.uid())
                .collection("anime").document(String.valueOf(id))
                .delete()
                .addOnSuccessListener(aVoid -> {
                    FirebaseUtils.firestore().collection("users")
                            .document(FirebaseUtils.uid())
                            .update("watchlistCount", FieldValue.increment(-1));

                    if (currentStatus.equals("completed")) {
                        FirebaseUtils.firestore().collection("users")
                                .document(FirebaseUtils.uid())
                                .update("watchedCount", FieldValue.increment(-1));
                    }
                    Toast.makeText(v.getContext(), "Removed from list", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return animeList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, score, status;
        ImageButton btnUpdate, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.animeImage);
            title = itemView.findViewById(R.id.animeTitle);
            score = itemView.findViewById(R.id.animeScore);
            status = itemView.findViewById(R.id.animeStatus);
            btnUpdate = itemView.findViewById(R.id.markCompleteButton);
            btnDelete = itemView.findViewById(R.id.deleteButton);
        }
    }
}
