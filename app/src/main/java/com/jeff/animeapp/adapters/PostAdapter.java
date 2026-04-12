package com.jeff.animeapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jeff.animeapp.R;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.Holder> {

    private final List<DocumentSnapshot> reviews;

    public PostAdapter(List<DocumentSnapshot> reviews) {
        this.reviews = reviews;
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
        DocumentSnapshot snapshot = reviews.get(position);

        String userName = snapshot.getString("username");
        String animeTitle = snapshot.getString("animeTitle");
        String content = snapshot.getString("reviewText");
        Double rating = snapshot.getDouble("rating");
        String avatarUrl = snapshot.getString("avatarUrl"); // optional if you store it

        holder.user.setText(userName != null ? userName : "Unknown");
        holder.animeTitle.setText(animeTitle != null ? animeTitle : "Anime");
        holder.content.setText(content != null ? content : "");
        holder.ratingBar.setRating(rating != null ? rating.floatValue() : 0f);

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.avatar);
        } else {
            holder.avatar.setImageResource(R.drawable.ic_profile);
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView user, animeTitle, content;
        ImageView avatar;
        RatingBar ratingBar;

        public Holder(@NonNull View itemView) {
            super(itemView);
            user = itemView.findViewById(R.id.userName);
            animeTitle = itemView.findViewById(R.id.animeTitle);
            content = itemView.findViewById(R.id.postContent);
            avatar = itemView.findViewById(R.id.userAvatar);
            ratingBar = itemView.findViewById(R.id.postRating);
        }
    }
}
