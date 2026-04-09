package com.jeff.animeapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jeff.animeapp.R;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.Holder> {

    private final JsonObject data;

    public PostAdapter(JsonObject data) {
        this.data = data;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        JsonArray arr = data.getAsJsonArray("data");
        if (arr == null || arr.size() <= position) return;

        JsonObject obj = arr.get(position).getAsJsonObject();

        // Defaults
        String userName = "Unknown";
        String animeTitle = "Anime";
        String content = "";
        String avatarUrl = null;
        int likes = 0;
        int dislikes = 0;
        float rating = 0f;

        if (obj.has("attributes")) {
            JsonObject attr = obj.getAsJsonObject("attributes");

            if (attr.has("content")) content = attr.get("content").getAsString();
            if (attr.has("animeTitle")) animeTitle = attr.get("animeTitle").getAsString();
            if (attr.has("likes")) likes = attr.get("likes").getAsInt();
            if (attr.has("dislikes")) dislikes = attr.get("dislikes").getAsInt();
            if (attr.has("rating")) rating = attr.get("rating").getAsFloat();

            if (attr.has("user")) {
                JsonObject user = attr.getAsJsonObject("user");
                if (user.has("name")) userName = user.get("name").getAsString();
                if (user.has("avatar")) avatarUrl = user.get("avatar").getAsString();
            }
        }

        // Bind data to UI
        holder.user.setText(userName);
        holder.animeTitle.setText(animeTitle);
        holder.content.setText(content);
        holder.ratingBar.setRating(rating);

        holder.likeButton.setText(likes + " Likes");
        holder.dislikeButton.setText(dislikes + " Dislikes");

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.avatar);
        } else {
            holder.avatar.setImageResource(R.drawable.ic_profile);
        }

        // Make final copies for lambdas
        final String finalAnimeTitle = animeTitle;
        final String finalUserName = userName;

        // Actions
        holder.likeButton.setOnClickListener(v ->
                Toast.makeText(v.getContext(), "Liked " + finalAnimeTitle, Toast.LENGTH_SHORT).show());

        holder.dislikeButton.setOnClickListener(v ->
                Toast.makeText(v.getContext(), "Disliked " + finalAnimeTitle, Toast.LENGTH_SHORT).show());

        holder.commentButton.setOnClickListener(v ->
                Toast.makeText(v.getContext(), "Reply to " + finalUserName, Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        try {
            return data.getAsJsonArray("data").size();
        } catch (Exception e) {
            return 0;
        }
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView user, animeTitle, content, likeButton, dislikeButton, commentButton;
        ImageView avatar;
        RatingBar ratingBar;

        public Holder(@NonNull View itemView) {
            super(itemView);
            user = itemView.findViewById(R.id.userName);
            animeTitle = itemView.findViewById(R.id.animeTitle);
            content = itemView.findViewById(R.id.postContent);
            avatar = itemView.findViewById(R.id.userAvatar);
            ratingBar = itemView.findViewById(R.id.postRating);
            likeButton = itemView.findViewById(R.id.likeButton);
            dislikeButton = itemView.findViewById(R.id.dislikeButton);
            commentButton = itemView.findViewById(R.id.commentButton);
        }
    }
}
