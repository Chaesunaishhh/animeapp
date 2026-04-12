package com.jeff.animeapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.jeff.animeapp.R;

public class LeaderboardFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        TextView tvTotal = v.findViewById(R.id.tvTotalScoreValue);
        TextView tvLatest = v.findViewById(R.id.tvYourScoreValue);

        SharedPreferences prefs = requireActivity().getSharedPreferences("QuizData", Context.MODE_PRIVATE);
        tvTotal.setText(String.valueOf(prefs.getInt("total_score", 0)));
        tvLatest.setText(String.valueOf(prefs.getInt("last_score", 0)));

        v.findViewById(R.id.btnTakeQuiz).setOnClickListener(view ->
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new QuizFragment())
                        .commit());

        return v;
    }
}