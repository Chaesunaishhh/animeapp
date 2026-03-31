package com.jeff.animeapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jeff.animeapp.R;
import com.jeff.animeapp.utils.FirebaseUtils;

import java.util.HashMap;

public class AnimeAdapter extends RecyclerView.Adapter<AnimeAdapter.ViewHolder> {

    private JsonArray animeList;

    public AnimeAdapter(JsonArray animeList) {
        this.animeList = animeList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_anime, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        JsonObject anime = animeList.get(position).getAsJsonObject();

        // Safe Title extraction
        String titleStr = "Unknown Title";
        if (anime.has("title") && anime.getAsJsonObject("title").has("romaji")) {
            titleStr = anime.getAsJsonObject("title").get("romaji").getAsString();
        }

        // Safe Image extraction
        String imageStr = "";
        if (anime.has("coverImage") && anime.getAsJsonObject("coverImage").has("large")) {
            imageStr = anime.getAsJsonObject("coverImage").get("large").getAsString();
        }

        // Safe Description extraction with HTML cleaning
        String descStr = "No description available.";
        if (anime.has("description") && !anime.get("description").isJsonNull()) {
            descStr = anime.get("description").getAsString().replaceAll("<.*?>", "");
        }

        // Safe Score extraction
        int scoreInt = 0;
        if (anime.has("averageScore") && !anime.get("averageScore").isJsonNull()) {
            scoreInt = anime.get("averageScore").getAsInt();
        }

        // Apply to UI
        holder.title.setText(titleStr);
        holder.score.setText("⭐ " + scoreInt);
        holder.desc.setText(descStr);

        Glide.with(holder.itemView.getContext())
                .load(imageStr)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.image);

        holder.btn.setOnClickListener(v -> addToWatchlist(holder.itemView, anime));
    }

    private void addToWatchlist(View view, JsonObject anime) {
        String uid = FirebaseUtils.uid();

        if (uid == null) {
            Toast.makeText(view.getContext(), "Please login to add to watchlist", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // FIX: Check if ID exists before calling getAsInt()
            int id;
            if (anime.has("id") && !anime.get("id").isJsonNull()) {
                id = anime.get("id").getAsInt();
            } else {
                // Fallback: use hashcode of title if ID is missing to prevent crash
                id = anime.getAsJsonObject("title").get("romaji").getAsString().hashCode();
            }

            String title = anime.getAsJsonObject("title").get("romaji").getAsString();
            String image = anime.getAsJsonObject("coverImage").get("large").getAsString();
            String desc = anime.has("description") ? anime.get("description").getAsString() : "";
            int score = anime.has("averageScore") && !anime.get("averageScore").isJsonNull()
                    ? anime.get("averageScore").getAsInt() : 0;

            HashMap<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("title", title);
            map.put("coverImage", image);
            map.put("description", desc);
            map.put("score", score);

            FirebaseFirestore.getInstance()
                    .collection("watchlist")
                    .document(uid)
                    .collection("anime")
                    .document(String.valueOf(id))
                    .set(map)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(view.getContext(), "Added to Watchlist!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(view.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } catch (Exception e) {
            Toast.makeText(view.getContext(), "Error processing anime data", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return animeList != null ? animeList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, score, desc;
        Button btn;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.animeImage);
            title = itemView.findViewById(R.id.animeTitle);
            score = itemView.findViewById(R.id.animeScore);
            desc = itemView.findViewById(R.id.animeDesc);
            // Matches the ID in your updated item_anime.xml
            btn = itemView.findViewById(R.id.btnWishlist);
        }
    }
}