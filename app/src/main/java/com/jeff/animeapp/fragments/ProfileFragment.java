package com.jeff.animeapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jeff.animeapp.R;
import com.jeff.animeapp.LoginActivity;

public class ProfileFragment extends Fragment {

    private Button logoutBtn;
    private TextView usernameView, emailView, statWatchedView, tvInWatchlist, tvQuizzesTaken, tvQuizAvgScore;
    private TextView progressCollector, progressMasterCollector, progressFinisher, progressMasterFinisher, progressLegendaryOtaku, progressQuizEnthusiast;
    private View collectorCard, masterCollectorCard, finisherCard, masterFinisherCard, legendaryOtakuCard, quizEnthusiastCard;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        logoutBtn = v.findViewById(R.id.logoutButton);
        usernameView = v.findViewById(R.id.profileUsername);
        emailView = v.findViewById(R.id.profileEmail);
        statWatchedView = v.findViewById(R.id.statWatched);
        tvInWatchlist = v.findViewById(R.id.tvInWatchlist);
        tvQuizzesTaken = v.findViewById(R.id.tvQuizzesTaken);
        tvQuizAvgScore = v.findViewById(R.id.tvQuizAvgScore);

        // Progress TextViews
        progressCollector = v.findViewById(R.id.progressCollector);
        progressMasterCollector = v.findViewById(R.id.progressMasterCollector);
        progressFinisher = v.findViewById(R.id.progressFinisher);
        progressMasterFinisher = v.findViewById(R.id.progressMasterFinisher);
        progressLegendaryOtaku = v.findViewById(R.id.progressLegendaryOtaku);
        progressQuizEnthusiast = v.findViewById(R.id.progressQuizEnthusiast);

        // Achievement cards
        collectorCard = v.findViewById(R.id.collectorCard);
        masterCollectorCard = v.findViewById(R.id.masterCollectorCard);
        finisherCard = v.findViewById(R.id.finisherCard);
        masterFinisherCard = v.findViewById(R.id.masterFinisherCard);
        legendaryOtakuCard = v.findViewById(R.id.legendaryOtakuCard);
        quizEnthusiastCard = v.findViewById(R.id.quizEnthusiastCard);

        logoutBtn.setOnClickListener(view -> {
            // Clear the local session
            SharedPreferences.Editor editor = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();

            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        });

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener((DocumentSnapshot doc) -> {
                        if (doc.exists()) {
                            String username = doc.getString("username");
                            String email = doc.getString("email");
                            Long watchedCount = doc.getLong("watchedCount");
                            Long watchlistCount = doc.getLong("watchlistCount");
                            Long quizCount = doc.getLong("quizCount");
                            Double quizAvgScore = doc.getDouble("quizAvgScore");

                            if (username != null) usernameView.setText(username);
                            if (email != null) emailView.setText(email);

                            // Statistics
                            statWatchedView.setText(watchedCount != null ? "Anime Watched: " + watchedCount : "Anime Watched: 0");
                            tvInWatchlist.setText(watchlistCount != null ? "In Watchlist: " + watchlistCount : "In Watchlist: 0");
                            tvQuizzesTaken.setText(quizCount != null ? "Quizzes Taken: " + quizCount: "Quizzes Taken: 0");
                            tvQuizAvgScore.setText(quizAvgScore != null ? "Quiz Avg Score: " + quizAvgScore + "%" : "Quiz Avg Score: 0%");

                            // Progress text updates
                            if (watchlistCount != null) {
                                progressCollector.setText("Progress: " + watchlistCount + "/10");
                                progressMasterCollector.setText("Progress: " + watchlistCount + "/50");
                            }
                            if (watchedCount != null) {
                                progressFinisher.setText("Progress: " + watchedCount + "/5");
                                progressMasterFinisher.setText("Progress: " + watchedCount + "/20");
                                progressLegendaryOtaku.setText("Progress: " + watchedCount + "/50");
                            }
                            if (quizCount != null) {
                                progressQuizEnthusiast.setText("Progress: " + quizCount + "/10");
                            }

                            // Achievements unlock logic
                            if (watchlistCount != null) {
                                if (watchlistCount >= 10) collectorCard.setBackgroundResource(R.drawable.achievement_unlocked);
                                if (watchlistCount >= 50) masterCollectorCard.setBackgroundResource(R.drawable.achievement_unlocked);
                            }
                            if (watchedCount != null) {
                                if (watchedCount >= 5) finisherCard.setBackgroundResource(R.drawable.achievement_unlocked);
                                if (watchedCount >= 20) masterFinisherCard.setBackgroundResource(R.drawable.achievement_unlocked);
                                if (watchedCount >= 50) legendaryOtakuCard.setBackgroundResource(R.drawable.achievement_unlocked);
                            }
                            if (quizCount != null && quizCount >= 10) {
                                quizEnthusiastCard.setBackgroundResource(R.drawable.achievement_unlocked);
                            }
                        }
                    });
        }

        return v;
    }
}
