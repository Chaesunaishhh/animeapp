package com.jeff.animeapp.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jeff.animeapp.R;
import com.jeff.animeapp.utils.FirebaseUtils;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

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
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .transform(new RoundedCorners(24))
                .into(holder.image);

        String status = anime.has("status") && !anime.get("status").isJsonNull()
                ? anime.get("status").getAsString()
                : "watching";

        holder.status.setText(status.toUpperCase());
        
        int bgRes;
        switch (status) {
            case "completed": bgRes = R.drawable.status_bg_completed; break;
            case "planning": bgRes = R.drawable.status_bg_planning; break;
            case "dropped": bgRes = R.drawable.status_bg_dropped; break;
            default: bgRes = R.drawable.status_bg_watching; break;
        }
        holder.status.setBackgroundResource(bgRes);

        holder.btnUpdate.setOnClickListener(v -> {
            String[] options = {"Watching", "Completed", "Planning", "Dropped"};
            new androidx.appcompat.app.AlertDialog.Builder(v.getContext(), R.style.AnimeAlertDialog)
                    .setTitle("Update Status")
                    .setItems(options, (dialog, which) -> {
                        String newStatus = options[which].toLowerCase();
                        FirebaseUtils.firestore().collection("watchlist")
                                .document(FirebaseUtils.uid())
                                .collection("anime").document(String.valueOf(id))
                                .update("status", newStatus)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(v.getContext(), "Status updated to " + options[which], Toast.LENGTH_SHORT).show();
                                    anime.addProperty("status", newStatus);
                                    
                                    // Update watchedCount if changed to/from completed
                                    if ("completed".equals(newStatus) && !"completed".equals(status)) {
                                        FirebaseUtils.firestore().collection("users")
                                                .document(FirebaseUtils.uid())
                                                .update("watchedCount", FieldValue.increment(1));
                                    } else if (!"completed".equals(newStatus) && "completed".equals(status)) {
                                        FirebaseUtils.firestore().collection("users")
                                                .document(FirebaseUtils.uid())
                                                .update("watchedCount", FieldValue.increment(-1));
                                    }
                                    
                                    notifyItemChanged(position);
                                });
                    })
                    .show();
        });

        holder.btnDelete.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(v.getContext(), R.style.AnimeAlertDialog)
                    .setTitle("Remove from Watchlist")
                    .setMessage("Are you sure you want to remove " + title + " from your watchlist?")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        FirebaseFirestore.getInstance().collection("watchlist")
                                .document(FirebaseUtils.uid())
                                .collection("anime").document(String.valueOf(id))
                                .get()
                                .addOnSuccessListener(doc -> {
                                    boolean wasCompleted = doc.exists() && "completed".equals(doc.getString("status"));

                                    FirebaseFirestore.getInstance().collection("watchlist")
                                            .document(FirebaseUtils.uid())
                                            .collection("anime").document(String.valueOf(id))
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {
                                                if (wasCompleted) {
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
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
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
        TextView title, score, status;
        ImageButton btnUpdate;
        ImageButton btnDelete;

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
