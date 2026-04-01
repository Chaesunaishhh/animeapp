package com.jeff.animeapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

        // Get the array of posts
        JsonArray arr = data.getAsJsonArray("data");
        if (arr == null || arr.size() <= position) return;

        JsonObject obj = arr.get(position).getAsJsonObject();

        // User info
        String userName = "Unknown";
        String avatarUrl = null;
        if (obj.has("attributes")) {
            JsonObject attr = obj.getAsJsonObject("attributes");

            // Optional content
            String content = attr.has("content") ? attr.get("content").getAsString() : "";
            holder.content.setText(content);

            // Optional user info
            if (attr.has("user")) {
                JsonObject user = attr.getAsJsonObject("user");
                userName = user.has("name") ? user.get("name").getAsString() : "Unknown";
                avatarUrl = user.has("avatar") ? user.get("avatar").getAsString() : null;
            }
        }

        holder.user.setText(userName);

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(avatarUrl)
                    .into(holder.avatar);
        }
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
        TextView user, content;
        ImageView avatar;

        public Holder(@NonNull View itemView) {
            super(itemView);
            user = itemView.findViewById(R.id.userName); // matches item_post.xml
            content = itemView.findViewById(R.id.postContent);
            avatar = itemView.findViewById(R.id.userAvatar); // matches item_post.xml
        }
    }
}