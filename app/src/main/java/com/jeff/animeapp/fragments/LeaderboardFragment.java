package com.jeff.animeapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jeff.animeapp.R;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardFragment extends Fragment {

    private String currentUsername;
    private RecyclerView recyclerLeaderboard;
    private LeaderboardAdapter adapter;
    private List<LeaderboardEntry> leaderboardList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        TextView tvTotal = v.findViewById(R.id.tvTotalScoreValue);
        TextView tvLatest = v.findViewById(R.id.tvYourScoreValue);
        TextView tvYourRank = v.findViewById(R.id.tvYourRank);
        recyclerLeaderboard = v.findViewById(R.id.recyclerLeaderboard);

        // ✅ Corrected Back Button setup
        View btnBack = v.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(view -> {
                getParentFragmentManager().popBackStack();
            });
        }

        SharedPreferences session = requireActivity()
                .getSharedPreferences("UserSession", Context.MODE_PRIVATE);

        currentUsername = session.getString("logged_in_user", null);

        if (currentUsername == null) {
            Toast.makeText(getContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
            return v;
        }

        SharedPreferences quizPrefs = requireActivity()
                .getSharedPreferences("QuizData", Context.MODE_PRIVATE);

        int latestScore = quizPrefs.getInt("last_" + currentUsername, 0);
        int totalScore = quizPrefs.getInt("total_" + currentUsername, 0);

        tvLatest.setText(String.valueOf(latestScore));
        tvTotal.setText(String.valueOf(totalScore));

        // Setup RecyclerView
        recyclerLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LeaderboardAdapter(leaderboardList, currentUsername);
        recyclerLeaderboard.setAdapter(adapter);

        loadGlobalTop10(tvYourRank);

        v.findViewById(R.id.btnBack).setOnClickListener(view -> {
            getParentFragmentManager().popBackStack();
        });

        return v;
    }

    // 🌍 GLOBAL TOP 10
    private void loadGlobalTop10(TextView tvYourRank) {
        FirebaseFirestore.getInstance()
                .collection("weekly_leaderboard")
                .orderBy("totalScore", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(snapshot -> {

                    leaderboardList.clear();
                    int rank = 1;
                    int yourRank = -1;

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String name = doc.getString("username");
                        Long score = doc.getLong("totalScore");

                        if (name != null && score != null) {
                            // Add medal emoji for top 3
                            String medal = "";
                            if (rank == 1) medal = "🥇 ";
                            else if (rank == 2) medal = "🥈 ";
                            else if (rank == 3) medal = "🥉 ";

                            LeaderboardEntry entry = new LeaderboardEntry(rank, medal + name, score);
                            leaderboardList.add(entry);

                            // Check if this is the current user
                            if (name.equals(currentUsername)) {
                                yourRank = rank;
                            }

                            rank++;
                        }
                    }

                    // Update your rank
                    if (yourRank != -1) {
                        tvYourRank.setText("Your Rank: #" + yourRank);
                    } else {
                        findUserGlobalRank(tvYourRank);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load leaderboard", Toast.LENGTH_SHORT).show()
                );
    }

    // 👤 FIND USER'S GLOBAL RANK (if not in top 10)
    private void findUserGlobalRank(TextView tvYourRank) {
        FirebaseFirestore.getInstance()
                .collection("weekly_leaderboard")
                .orderBy("totalScore", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    int rank = 1;
                    for (var doc : snapshot) {
                        String name = doc.getString("username");
                        if (name != null && name.equals(currentUsername)) {
                            tvYourRank.setText("Your Rank: #" + rank);
                            return;
                        }
                        rank++;
                    }
                    tvYourRank.setText("Your Rank: Unranked (Take a quiz!)");
                })
                .addOnFailureListener(e ->
                        tvYourRank.setText("Your Rank: Unavailable")
                );
    }

    // Adapter class for RecyclerView
    private static class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

        private List<LeaderboardEntry> entries;
        private String currentUsername;

        public LeaderboardAdapter(List<LeaderboardEntry> entries, String currentUsername) {
            this.entries = entries;
            this.currentUsername = currentUsername;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_leaderboard, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LeaderboardEntry entry = entries.get(position);
            holder.bind(entry, currentUsername);
        }

        @Override
        public int getItemCount() {
            return entries.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tvRank;
            private TextView tvName;
            private TextView tvScore;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvRank = itemView.findViewById(R.id.tvRank);
                tvName = itemView.findViewById(R.id.tvName);
                tvScore = itemView.findViewById(R.id.tvScore);
            }

            public void bind(LeaderboardEntry entry, String currentUsername) {
                tvRank.setText("#" + entry.rank);
                tvName.setText(entry.username);
                tvScore.setText(String.valueOf(entry.score));

                // Highlight current user
                String cleanName = entry.username.replace("🥇 ", "").replace("🥈 ", "").replace("🥉 ", "");
                if (cleanName.equals(currentUsername)) {
                    ((androidx.cardview.widget.CardView) itemView).setCardBackgroundColor(0xFF2E2E3E);
                } else {
                    ((androidx.cardview.widget.CardView) itemView).setCardBackgroundColor(0xFF1E1E2C);
                }
            }
        }
    }

    // Data class for leaderboard entries
    private static class LeaderboardEntry {
        int rank;
        String username;
        long score;

        public LeaderboardEntry(int rank, String username, long score) {
            this.rank = rank;
            this.username = username;
            this.score = score;
        }
    }
}