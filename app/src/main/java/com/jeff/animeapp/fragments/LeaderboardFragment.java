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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

        // Load Local Stats
        SharedPreferences prefs = requireActivity().getSharedPreferences("QuizData", Context.MODE_PRIVATE);
        tvLatest.setText(String.valueOf(prefs.getInt("last_score", 0)));
        tvTotal.setText(String.valueOf(prefs.getInt("total_score", 0)));

        // --- DAILY LOCK CHECK ---
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        if (prefs.getString("last_played_date", "").equals(today)) {
            btnTakeQuiz.setEnabled(false);
            btnTakeQuiz.setText("Limit Reached");
            btnTakeQuiz.setAlpha(0.5f);
        } else {
            btnTakeQuiz.setOnClickListener(view -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new QuizFragment())
                        .commit();
            });
        }

        // --- FIREBASE TOP 3 FETCH ---
        FirebaseFirestore.getInstance().collection("users")
                .orderBy("highScore", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return;
                    for (int i = 0; i < snapshots.size(); i++) {
                        String name = snapshots.getDocuments().get(i).getString("username");
                        Long score = snapshots.getDocuments().get(i).getLong("highScore");
                        String rankData = name + " - " + (score != null ? score : 0);

                        if (i == 0) tvTop1.setText("🥇 " + rankData);
                        else if (i == 1) tvTop2.setText("🥈 " + rankData);
                        else if (i == 2) tvTop3.setText("🥉 " + rankData);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load ranks", Toast.LENGTH_SHORT).show());

        return v;
    }
}