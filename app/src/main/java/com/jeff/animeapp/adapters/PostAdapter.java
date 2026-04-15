package com.jeff.animeapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jeff.animeapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.Holder> {

    private List<DocumentSnapshot> reviews;
    private String currentUser;

    public PostAdapter(List<DocumentSnapshot> reviews) {
        this(reviews, "Guest");
    }

    public PostAdapter(List<DocumentSnapshot> reviews, String currentUser) {
        this.reviews = reviews != null ? reviews : new ArrayList<>();
        this.currentUser = currentUser;
    }

    public void updateData(List<DocumentSnapshot> newReviews) {
        this.reviews = newReviews != null ? newReviews : new ArrayList<>();
        notifyDataSetChanged();
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
        try {
            DocumentSnapshot snapshot = reviews.get(position);

            if (!snapshot.exists()) {
                return;
            }

            // Get fields
            String userName = snapshot.getString("username");
            String animeTitle = snapshot.getString("animeTitle");
            String content = snapshot.getString("reviewText");
            Double rating = snapshot.getDouble("rating");
            Long timestamp = snapshot.getLong("timestamp");
            String avatarUrl = snapshot.getString("avatarUrl");

            // Set text values
            holder.user.setText(userName != null ? userName : "Anonymous");
            holder.animeTitle.setText(animeTitle != null ? animeTitle : "Unknown Anime");
            holder.content.setText(content != null ? content : "No review content");

            // Set rating
            float ratingValue = rating != null ? rating.floatValue() : 0f;
            holder.ratingBar.setRating(ratingValue);

            // Set rating text (e.g., "4.5/5")
            if (holder.ratingText != null) {
                holder.ratingText.setText(String.format(Locale.getDefault(), "%.1f/5", ratingValue));
            }

            // Set timestamp
            if (holder.timestamp != null && timestamp != null) {
                holder.timestamp.setText(formatTimestamp(timestamp));
            }

            // Likes Logic
            List<String> likes = (List<String>) snapshot.get("likes");
            if (likes == null) likes = new ArrayList<>();
            int likeCount = likes.size();
            boolean isLiked = likes.contains(currentUser);

            holder.likeCount.setText(String.valueOf(likeCount));
            holder.likeButton.setImageResource(isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            
            int activeColor = holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark);
            int inactiveColor = holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray);
            holder.likeButton.setColorFilter(isLiked ? activeColor : inactiveColor);
            holder.likeCount.setTextColor(isLiked ? activeColor : inactiveColor);

            holder.layoutLike.setOnClickListener(v -> {
                if (currentUser.equals("Guest")) {
                    Toast.makeText(v.getContext(), "Please login to like reviews", Toast.LENGTH_SHORT).show();
                    return;
                }
                holder.layoutLike.setEnabled(false); // Prevent multiple clicks during update
                handleLike(snapshot.getId(), isLiked, holder);
            });

            // Delete logic (if user is the owner)
            holder.itemView.setOnLongClickListener(v -> {
                if (userName != null && userName.equals(currentUser)) {
                    showDeleteDialog(snapshot.getId(), holder.itemView.getContext());
                    return true;
                }
                return false;
            });

            // Load avatar with Glide
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .circleCrop()
                        .into(holder.avatar);
            } else {
                holder.avatar.setImageResource(R.drawable.ic_profile);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLike(String reviewId, boolean currentlyLiked, Holder holder) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (currentlyLiked) {
            db.collection("reviews").document(reviewId)
                    .update("likes", FieldValue.arrayRemove(currentUser))
                    .addOnCompleteListener(task -> {
                        if (holder != null) holder.layoutLike.setEnabled(true);
                    });
        } else {
            db.collection("reviews").document(reviewId)
                    .update("likes", FieldValue.arrayUnion(currentUser))
                    .addOnCompleteListener(task -> {
                        if (holder != null) holder.layoutLike.setEnabled(true);
                    });
        }
    }

    private void showDeleteDialog(String reviewId, android.content.Context context) {
        new AlertDialog.Builder(context, R.style.CustomAlertDialog)
                .setTitle("Delete Review")
                .setMessage("Are you sure you want to delete this review?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    FirebaseFirestore.getInstance().collection("reviews")
                            .document(reviewId)
                            .delete()
                            .addOnSuccessListener(aVoid -> 
                                Toast.makeText(context, "Review deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> 
                                Toast.makeText(context, "Error deleting review", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String formatTimestamp(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        // Less than 1 minute
        if (diff < 60 * 1000) {
            return "Just now";
        }
        // Less than 1 hour
        if (diff < 60 * 60 * 1000) {
            long minutes = diff / (60 * 1000);
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        }
        // Less than 24 hours
        if (diff < 24 * 60 * 60 * 1000) {
            long hours = diff / (60 * 60 * 1000);
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        }
        // Less than 7 days
        if (diff < 7 * 24 * 60 * 60 * 1000) {
            long days = diff / (24 * 60 * 60 * 1000);
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        }

        // Format date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView user, animeTitle, content, timestamp, ratingText, likeCount;
        ImageView avatar, likeButton;
        RatingBar ratingBar;
        View layoutLike;

        public Holder(@NonNull View itemView) {
            super(itemView);
            user = itemView.findViewById(R.id.userName);
            animeTitle = itemView.findViewById(R.id.animeTitle);
            content = itemView.findViewById(R.id.postContent);
            avatar = itemView.findViewById(R.id.userAvatar);
            ratingBar = itemView.findViewById(R.id.postRating);
            timestamp = itemView.findViewById(R.id.timestamp);
            ratingText = itemView.findViewById(R.id.ratingText);
            likeButton = itemView.findViewById(R.id.likeButton);
            likeCount = itemView.findViewById(R.id.likeCount);
            layoutLike = itemView.findViewById(R.id.layoutLike);
        }
    }
}