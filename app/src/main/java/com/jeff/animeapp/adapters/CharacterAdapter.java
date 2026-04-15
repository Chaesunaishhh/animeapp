package com.jeff.animeapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jeff.animeapp.R;

import java.util.List;

public class CharacterAdapter extends RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder> {

    private List<DocumentSnapshot> characterList;
    private String currentUser;
    private int maxVotes = 1; // Used to calculate progress bar percentage

    public CharacterAdapter(List<DocumentSnapshot> characterList, String currentUser) {
        this.characterList = characterList;
        this.currentUser = currentUser;

        // Find max votes for scaling the progress bars
        for (DocumentSnapshot doc : characterList) {
            Long votes = doc.getLong("votes");
            if (votes != null && votes > maxVotes) {
                maxVotes = votes.intValue();
            }
        }
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

        String name = doc.getString("name");
        String anime = doc.getString("anime");
        String imageUrl = doc.getString("imageUrl");
        Long votes = doc.getLong("votes");
        long voteCount = votes != null ? votes : 0;

        // Rank number (1-based)
        holder.tvRank.setText(String.valueOf(position + 1));

        // Character name & anime
        holder.tvName.setText(name != null ? name : "Unknown");
        holder.tvAnime.setText(anime != null ? anime : "");

        // Vote count label
        holder.tvVotes.setText(voteCount + " votes");

        // Progress bar scaled to max votes
        int progress = maxVotes > 0 ? (int) ((voteCount * 100L) / maxVotes) : 0;
        holder.progressVotes.setProgress(progress);

        // Load character image
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .circleCrop()
                    .into(holder.ivCharacter);
        } else {
            holder.ivCharacter.setImageResource(R.drawable.ic_launcher_background);
        }

        // Check if current user already voted for this character
        List<String> voters = (List<String>) doc.get("voters");
        boolean hasVoted = voters != null && voters.contains(currentUser);
        holder.btnVote.setSelected(hasVoted);
        holder.btnVote.setImageResource(hasVoted ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);

        // Vote button click
        holder.btnVote.setOnClickListener(v -> {
            if (hasVoted) {
                Toast.makeText(v.getContext(), "You already voted for " + name + "!", Toast.LENGTH_SHORT).show();
                return;
            }
            castVote(doc.getId(), name, holder, position);
        });
    }

    private void castVote(String docId, String characterName, CharacterViewHolder holder, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("characters").document(docId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Long currentVotes = snapshot.getLong("votes");
                    long newVotes = (currentVotes != null ? currentVotes : 0) + 1;

                    // Update votes and add user to voters list
                    db.collection("characters").document(docId)
                            .update(
                                    "votes", newVotes,
                                    "voters", com.google.firebase.firestore.FieldValue.arrayUnion(currentUser)
                            )
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(holder.itemView.getContext(),
                                        "Voted for " + characterName + "!", Toast.LENGTH_SHORT).show();
                                // Mark button as voted
                                holder.btnVote.setSelected(true);
                                holder.btnVote.setImageResource(R.drawable.ic_heart_filled);
                                holder.tvVotes.setText(newVotes + " votes");

                                // Update progress
                                if (newVotes > maxVotes) maxVotes = (int) newVotes;
                                int progress = (int) ((newVotes * 100L) / maxVotes);
                                holder.progressVotes.setProgress(progress);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(holder.itemView.getContext(),
                                            "Vote failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
    }

    @Override
    public int getItemCount() {
        return characterList.size();
    }

    public static class CharacterViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvAnime, tvVotes;
        ImageView ivCharacter;
        ProgressBar progressVotes;
        ImageButton btnVote;

        public CharacterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvName = itemView.findViewById(R.id.tvName);
            tvAnime = itemView.findViewById(R.id.tvAnime);
            tvVotes = itemView.findViewById(R.id.tvVotes);
            ivCharacter = itemView.findViewById(R.id.ivCharacter);
            progressVotes = itemView.findViewById(R.id.progressVotes);
            btnVote = itemView.findViewById(R.id.btnVote);
        }
    }
}