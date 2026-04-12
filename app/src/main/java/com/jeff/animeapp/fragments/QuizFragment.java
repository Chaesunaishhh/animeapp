package com.jeff.animeapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jeff.animeapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class QuizFragment extends Fragment {

    private TextView tvQuestion, tvScore, tvQuestionCount, tvLives;
    private ProgressBar progressBar;
    private final Button[] options = new Button[4];

    private int score = 0;
    private int currentIdx = 0;
    private int lives = 3;
    private boolean isBonus = false;

    // Added 'final' to fix your IDE warnings
    private final String[] questions = {
            "Who is the protagonist of 'Attack on Titan'?", "What is Goku's signature move?",
            "Who is the 'Copy Ninja' in Naruto?", "Luffy's Devil Fruit is the...?",
            "What is the Shinigami's name in 'Death Note'?", "Which anime features Nezuko?",
            "Who is the Fullmetal Alchemist?", "What is Saitama's hero name?",
            "Who is the King of Curses in JJK?", "The world of Sword Art Online is called...?"
    };

    private final String[][] choices = {
            {"Levi", "Eren Yeager", "Mikasa", "Armin"}, {"Chidori", "Rasengan", "Kamehameha", "Bankai"},
            {"Kakashi", "Sasuke", "Itachi", "Minato"}, {"Gum-Gum", "Flame-Flame", "Ice-Ice", "Dark-Dark"},
            {"Ryuk", "Rem", "Light", "L"}, {"Black Clover", "Demon Slayer", "Bleach", "Fate"},
            {"Alphonse", "Edward Elric", "Mustang", "Scar"}, {"Caped Baldy", "One Punch Man", "Silver Fang", "Genos"},
            {"Gojo", "Sukuna", "Mahito", "Jogo"}, {"Aincrad", "Alfheim", "Gun Gale", "Underworld"}
    };

    private final String[] answers = {
            "Eren Yeager", "Kamehameha", "Kakashi", "Gum-Gum", "Ryuk",
            "Demon Slayer", "Edward Elric", "Caped Baldy", "Sukuna", "Aincrad"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // 1. DAILY LOCK: Check if already played today
        SharedPreferences prefs = requireActivity().getSharedPreferences("QuizData", Context.MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (prefs.getString("last_played_date", "").equals(today)) {
            Toast.makeText(getContext(), "You've already played today! Come back tomorrow.", Toast.LENGTH_LONG).show();
            navigateToLeaderboard();
            return new View(getContext());
        }

        View v = inflater.inflate(R.layout.fragment_quiz, container, false);

        // Initialize Views
        tvQuestion = v.findViewById(R.id.tvQuestion);
        tvScore = v.findViewById(R.id.tvScore);
        tvQuestionCount = v.findViewById(R.id.tvQuestionCount);
        tvLives = v.findViewById(R.id.tvLives);
        progressBar = v.findViewById(R.id.quizProgress);

        options[0] = v.findViewById(R.id.btnOption1);
        options[1] = v.findViewById(R.id.btnOption2);
        options[2] = v.findViewById(R.id.btnOption3);
        options[3] = v.findViewById(R.id.btnOption4);

        updateUI();

        for (Button btn : options) {
            btn.setOnClickListener(view -> checkAnswer(((Button)view).getText().toString()));
        }

        return v;
    }

    private void updateUI() {
        if (currentIdx >= questions.length) return;

        progressBar.setProgress(currentIdx + 1);
        tvQuestionCount.setText("Question " + (currentIdx + 1) + " of 10");
        tvScore.setText("Score: " + score);

        // Show hearts based on lives
        StringBuilder hearts = new StringBuilder();
        for(int i=0; i<lives; i++) hearts.append("❤️");
        tvLives.setText(hearts.toString());

        // Random Bonus Logic
        isBonus = new Random().nextInt(5) == 0;
        if (isBonus) {
            tvQuestion.setText("🔥 BONUS (+20): " + questions[currentIdx]);
            tvQuestion.setTextColor(Color.parseColor("#FFD700")); // Gold
        } else {
            tvQuestion.setText(questions[currentIdx]);
            tvQuestion.setTextColor(Color.WHITE);
        }

        for (int i = 0; i < 4; i++) options[i].setText(choices[currentIdx][i]);
    }

    private void checkAnswer(String selected) {
        if (selected.equals(answers[currentIdx])) {
            score += isBonus ? 20 : 10;
        } else {
            lives--;
        }

        if (lives <= 0 || currentIdx >= 9) {
            saveAndNavigate();
        } else {
            currentIdx++;
            updateUI();
        }
    }

    private void saveAndNavigate() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        SharedPreferences prefs = requireActivity().getSharedPreferences("QuizData", Context.MODE_PRIVATE);

        prefs.edit()
                .putInt("last_score", score)
                .putString("last_played_date", today)
                .apply();

        navigateToLeaderboard();
    }

    private void navigateToLeaderboard() {
        if (isAdded()) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new LeaderboardFragment())
                    .commit();
        }
    }
}