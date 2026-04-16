package com.jeff.animeapp.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.jeff.animeapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharacterAdapter extends RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder> {

    private List<DocumentSnapshot> characterList;
    private String currentUser;
    private int maxVotes = 1;
    private Map<String, Integer> userVotes = new HashMap<>();

    public CharacterAdapter(List<DocumentSnapshot> characterList, String currentUser) {
        this.characterList = characterList != null ? characterList : new ArrayList<>();
        this.currentUser = currentUser;
        calculateMaxVotes();
        loadUserVotes();
    }

    private void loadUserVotes() {
        int voteCount = 0;
        for (DocumentSnapshot doc : characterList) {
            List<String> voters = (List<String>) doc.get("voters");
            if (voters != null && voters.contains(currentUser)) {
                voteCount++;
                userVotes.put(doc.getId(), 1);
            }
        }
    }

    private void calculateMaxVotes() {
        maxVotes = 1;
        for (DocumentSnapshot doc : characterList) {
            if (doc != null && doc.exists()) {
                Long votes = doc.getLong("votes");
                if (votes != null && votes > maxVotes) {
                    maxVotes = votes.intValue();
                }
            }
        }
    }

    public void updateData(List<DocumentSnapshot> newList) {
        this.characterList = newList != null ? newList : new ArrayList<>();
        calculateMaxVotes();
        loadUserVotes();
        notifyDataSetChanged();
    }

    public int getUserVoteCount() {
        int count = 0;
        for (DocumentSnapshot doc : characterList) {
            List<String> voters = (List<String>) doc.get("voters");
            if (voters != null && voters.contains(currentUser)) {
                count++;
            }
        }
        return count;
    }

    @NonNull
    @Override
    public CharacterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_character, parent, false);
        return new CharacterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CharacterViewHolder holder, int position) {
        DocumentSnapshot doc = characterList.get(position);

        if (!doc.exists()) {
            return;
        }

        String title = doc.getString("name");
        String type = doc.getString("anime");
        String imageUrl = doc.getString("imageUrl");

        Long votes = doc.getLong("votes");
        long voteCount = (votes != null) ? votes : 0;

        // Rank with medals
        String rankText = (position + 1) + "";
        if (position == 0) {
            rankText = "🥇 " + rankText;
        } else if (position == 1) {
            rankText = "🥈 " + rankText;
        } else if (position == 2) {
            rankText = "🥉 " + rankText;
        }
        holder.tvRank.setText(rankText);

        // Text
        holder.tvTitle.setText(title != null ? title : "Unknown");
        holder.tvType.setText(type != null ? type : "");
        holder.tvVotes.setText(voteCount + " vote" + (voteCount != 1 ? "s" : ""));

        // Progress bar
        int progress = maxVotes > 0 ? (int) ((voteCount * 100L) / maxVotes) : 0;
        holder.progressVotes.setProgress(progress);

        if (imageUrl != null && !imageUrl.isEmpty() && (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {

            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .timeout(15000);

            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .apply(requestOptions)
                    .into(holder.ivPoster);
        } else {
            holder.ivPoster.setImageResource(R.drawable.ic_launcher_background);
            holder.ivPoster.setBackgroundColor(getColorForName(title));
        }

        // Check if user has voted for this character
        List<String> voters = (List<String>) doc.get("voters");
        boolean hasVoted = voters != null && voters.contains(currentUser);
        int userVoteCount = getUserVoteCount();

        boolean canInteract = !currentUser.equals("Guest") && (hasVoted || userVoteCount < 3);

        holder.btnVote.setImageResource(
                hasVoted ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline
        );

        if (!canInteract && !currentUser.equals("Guest")) {
            holder.btnVote.setAlpha(0.3f);
        } else {
            holder.btnVote.setAlpha(1.0f);
        }

        holder.btnVote.setOnClickListener(v -> {
            if (currentUser.equals("Guest")) {
                Toast.makeText(v.getContext(),
                        "Please login to vote!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (hasVoted) {
                removeVote(doc.getId(), title, holder);
            } else {
                if (userVoteCount >= 3) {
                    Toast.makeText(v.getContext(),
                            "You've used all 3 votes! Remove one to vote for " + title,
                            Toast.LENGTH_LONG).show();
                } else {
                    castVote(doc.getId(), title, holder);
                }
            }
        });
    }

    private int getColorForName(String name) {
        if (name == null) return 0xFF4CAF50;

        int hash = name.hashCode();
        int[] colors = {
                0xFF4CAF50, 0xFF2196F3, 0xFF9C27B0, 0xFFFF9800,
                0xFFF44336, 0xFF009688, 0xFF673AB7, 0xFFFF5722,
                0xFF795548, 0xFF607D8B, 0xFFE91E63, 0xFF00BCD4
        };
        return colors[Math.abs(hash) % colors.length];
    }

    private void removeVote(String docId, String characterName, CharacterViewHolder holder) {
        holder.btnVote.setEnabled(false);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("characters").document(docId)
                .update(
                        "votes", FieldValue.increment(-1),
                        "voters", FieldValue.arrayRemove(currentUser)
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(holder.itemView.getContext(),
                            "Removed vote for " + characterName,
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(holder.itemView.getContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    holder.btnVote.setEnabled(true);
                });
    }

    private void castVote(String docId, String characterName, CharacterViewHolder holder) {
        holder.btnVote.setEnabled(false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Check total votes this week for this user across all characters
        db.collection("characters").whereArrayContains("voters", currentUser).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.size() >= 3) {
                        Toast.makeText(holder.itemView.getContext(),
                                "You've already used your 3 votes this week! Remove a vote to change.",
                                Toast.LENGTH_LONG).show();
                        holder.btnVote.setEnabled(true);
                        return;
                    }

                    // 2. Double-check if already voted for THIS character (race condition check)
                    db.collection("characters").document(docId).get().addOnSuccessListener(doc -> {
                        List<String> voters = (List<String>) doc.get("voters");
                        if (voters != null && voters.contains(currentUser)) {
                            Toast.makeText(holder.itemView.getContext(), "Already voted!", Toast.LENGTH_SHORT).show();
                            holder.btnVote.setEnabled(true);
                            return;
                        }

                        // 3. Perform the vote update
                        db.collection("characters").document(docId)
                                .update(
                                        "votes", FieldValue.increment(1),
                                        "voters", FieldValue.arrayUnion(currentUser)
                                )
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(holder.itemView.getContext(),
                                            "✓ Voted for " + characterName + "!",
                                            Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(holder.itemView.getContext(),
                                            "✗ Vote failed: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                    holder.btnVote.setEnabled(true);
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(holder.itemView.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    holder.btnVote.setEnabled(true);
                });
    }

    @Override
    public int getItemCount() {
        return characterList.size();
    }

    static class CharacterViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvTitle, tvType, tvVotes;
        ImageView ivPoster;
        ProgressBar progressVotes;
        ImageButton btnVote;

        public CharacterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvType = itemView.findViewById(R.id.tvType);
            tvVotes = itemView.findViewById(R.id.tvVotes);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            progressVotes = itemView.findViewById(R.id.progressVotes);
            btnVote = itemView.findViewById(R.id.btnVote);
        }
    }
}