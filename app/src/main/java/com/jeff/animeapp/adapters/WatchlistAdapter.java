package com.jeff.animeapp.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jeff.animeapp.R;
import com.jeff.animeapp.utils.FirebaseUtils;
import com.google.firebase.firestore.FieldValue;

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

        String title = anime.has("title") && anime.getAsJsonObject("title").has("romaji")
                ? anime.getAsJsonObject("title").get("romaji").getAsString()
                : "Unknown";

        String image = anime.has("coverImage") && anime.getAsJsonObject("coverImage").has("large")
                ? anime.getAsJsonObject("coverImage").get("large").getAsString()
                : "";

        int score = anime.has("averageScore") && !anime.get("averageScore").isJsonNull()
                ? anime.get("averageScore").getAsInt()
                : 0;

        int id = anime.has("id") && !anime.get("id").isJsonNull()
                ? anime.get("id").getAsInt()
                : 0;

        holder.title.setText(title);
        holder.score.setText("⭐ " + score);

        Glide.with(holder.itemView.getContext())
                .load(image)
                .transform(new RoundedCorners(24))
                .into(holder.image);

        String status = anime.has("status") && !anime.get("status").isJsonNull()
                ? anime.get("status").getAsString()
                : "watching";

        if ("completed".equals(status)) {
            holder.btnDone.setText("Completed");
            holder.btnDone.setEnabled(false);
            holder.btnDone.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#4CAF50"))
            );
        } else {
            holder.btnDone.setText("Complete");
            holder.btnDone.setEnabled(true);
            holder.btnDone.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#6200EE"))
            );
        }

        // Done button → mark as completed + increment watchedCount
        holder.btnDone.setOnClickListener(v -> {
            FirebaseUtils.firestore().collection("watchlist")
                    .document(FirebaseUtils.uid())
                    .collection("anime").document(String.valueOf(id))
                    .update("status", "completed")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(v.getContext(), "Marked as Completed!", Toast.LENGTH_SHORT).show();
                        anime.addProperty("status", "completed");
                        notifyItemChanged(position);

                        // Increment watchedCount in user profile
                        FirebaseUtils.firestore().collection("users")
                                .document(FirebaseUtils.uid())
                                .update("watchedCount", FieldValue.increment(1));
                    });
        });

        // Delete button → remove from Firestore + decrement if completed
        holder.btnDelete.setOnClickListener(v -> {
            String currentStatus = anime.has("status") && !anime.get("status").isJsonNull()
                    ? anime.get("status").getAsString()
                    : "watching";

            FirebaseUtils.firestore().collection("watchlist")
                    .document(FirebaseUtils.uid())
                    .collection("anime").document(String.valueOf(id))
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Decrement watchedCount if anime was completed
                        if ("completed".equals(currentStatus)) {
                            FirebaseUtils.firestore().collection("users")
                                    .document(FirebaseUtils.uid())
                                    .update("watchedCount", FieldValue.increment(-1));
                        }

                        animeList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, animeList.size());
                        Toast.makeText(v.getContext(), "Removed from Watchlist!", Toast.LENGTH_SHORT).show();
                    });
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAnimeClick(id);
        });
    }

    @Override
    public int getItemCount() {
        return animeList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, score;
        MaterialButton btnDone, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.animeImage);
            title = itemView.findViewById(R.id.animeTitle);
            score = itemView.findViewById(R.id.animeScore);
            btnDone = itemView.findViewById(R.id.btnDone);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
