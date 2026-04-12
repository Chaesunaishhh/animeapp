package com.jeff.animeapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.jeff.animeapp.R;

public class LeaderboardFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        // Bind Views
        TextView tvTotal = v.findViewById(R.id.tvTotalScoreValue);
        TextView tvLatest = v.findViewById(R.id.tvYourScoreValue);
        TextView tvTop1 = v.findViewById(R.id.tvTop1Name);
        TextView tvTop2 = v.findViewById(R.id.tvTop2Name);
        TextView tvTop3 = v.findViewById(R.id.tvTop3Name);
        Button btnTakeQuiz = v.findViewById(R.id.btnTakeQuiz);

        // Get Account Info
        SharedPreferences userSession = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String currentUsername = userSession.getString("logged_in_user", "Guest");

        // Load Locally Saved Stats (Specific to current user)
        SharedPreferences quizPrefs = requireActivity().getSharedPreferences("QuizData", Context.MODE_PRIVATE);
        tvLatest.setText(String.valueOf(quizPrefs.getInt(currentUsername + "_last_score", 0)));
        tvTotal.setText(String.valueOf(quizPrefs.getInt(currentUsername + "_total_score", 0)));

        btnTakeQuiz.setOnClickListener(view -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new QuizFragment())
                    .commit();
        });

        // Fetch Global Rankings
        fetchLeaderboard(tvTop1, tvTop2, tvTop3);

        return v;
    }

    private void fetchLeaderboard(TextView t1, TextView t2, TextView t3) {
        FirebaseFirestore.getInstance().collection("users")
                .orderBy("highScore", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return;

                    // Set placeholders if DB is empty
                    t1.setText("🥇 ---");
                    t2.setText("🥈 ---");
                    t3.setText("🥉 ---");

                    for (int i = 0; i < snapshots.size(); i++) {
                        String name = snapshots.getDocuments().get(i).getString("username");
                        Long highScore = snapshots.getDocuments().get(i).getLong("highScore");
                        String display = (name != null ? name : "User") + " - " + (highScore != null ? highScore : 0);

                        if (i == 0) t1.setText("🥇 " + display);
                        else if (i == 1) t2.setText("🥈 " + display);
                        else if (i == 2) t3.setText("🥉 " + display);
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(getContext(), "Error syncing leaderboard", Toast.LENGTH_SHORT).show();
                });
    }
}