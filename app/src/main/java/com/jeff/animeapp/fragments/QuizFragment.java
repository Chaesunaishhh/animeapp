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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.jeff.animeapp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class QuizFragment extends Fragment {

    private TextView tvQuestion, tvScore, tvQuestionCount, tvLives;
    private ProgressBar progressBar;
    private final Button[] options = new Button[4];

    private int score = 0;
    private int currentIdx = 0;
    private int lives = 3;
    private boolean isBonus = false;
    private String currentUsername;

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

    private List<Integer> questionIndices;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SharedPreferences userSession = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUsername = userSession.getString("logged_in_user", "Guest");

        View v = inflater.inflate(R.layout.fragment_quiz, container, false);

        tvQuestion = v.findViewById(R.id.tvQuestion);
        tvScore = v.findViewById(R.id.tvScore);
        tvQuestionCount = v.findViewById(R.id.tvQuestionCount);
        tvLives = v.findViewById(R.id.tvLives);
        progressBar = v.findViewById(R.id.quizProgress);
        progressBar.setMax(10);

        options[0] = v.findViewById(R.id.btnOption1);
        options[1] = v.findViewById(R.id.btnOption2);
        options[2] = v.findViewById(R.id.btnOption3);
        options[3] = v.findViewById(R.id.btnOption4);

        setupRandomQuestions();
        updateUI();

        for (Button btn : options) {
            btn.setOnClickListener(view -> checkAnswer(((Button)view).getText().toString()));
        }

        return v;
    }

    private void setupRandomQuestions() {
        questionIndices = new ArrayList<>();
        for (int i = 0; i < questions.length; i++) questionIndices.add(i);
        Collections.shuffle(questionIndices);
    }

    private void updateUI() {
        if (currentIdx >= 10) return;

        int actualIdx = questionIndices.get(currentIdx);
        tvScore.setText("Score: " + score);
        tvQuestionCount.setText("Question " + (currentIdx + 1) + " of 10");
        progressBar.setProgress(currentIdx + 1);

        StringBuilder hearts = new StringBuilder();
        for(int i=0; i<lives; i++) hearts.append("❤️");
        tvLives.setText(hearts.toString());

        isBonus = new Random().nextInt(5) == 0;
        if (isBonus) {
            tvQuestion.setText("🔥 BONUS (+20): " + questions[actualIdx]);
            tvQuestion.setTextColor(Color.parseColor("#FFD700"));
        } else {
            tvQuestion.setText(questions[actualIdx]);
            tvQuestion.setTextColor(Color.WHITE);
        }

        List<String> currentChoices = new ArrayList<>();
        for (String s : choices[actualIdx]) currentChoices.add(s);
        Collections.shuffle(currentChoices);

        for (int i = 0; i < 4; i++) options[i].setText(currentChoices.get(i));
    }

    private void checkAnswer(String selected) {
        int actualIdx = questionIndices.get(currentIdx);
        if (selected.equals(answers[actualIdx])) {
            score += isBonus ? 20 : 10;
        } else {
            lives--;
        }

        currentIdx++;
        if (lives <= 0 || currentIdx >= 10) {
            saveAndNavigate();
        } else {
            updateUI();
        }
    }

    private void saveAndNavigate() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("QuizData", Context.MODE_PRIVATE);

        // Accumulate Total Score (Old Total + New Score)
        int currentTotal = prefs.getInt(currentUsername + "_total_score", 0);
        int newTotal = currentTotal + score;

        prefs.edit()
                .putInt(currentUsername + "_last_score", score)
                .putInt(currentUsername + "_total_score", newTotal)
                .apply();

        // Save to Firebase for the Global Leaderboard
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("username", currentUsername);
        userUpdate.put("highScore", score);

        db.collection("users").document(currentUsername)
                .set(userUpdate)
                .addOnCompleteListener(task -> navigateToLeaderboard());
    }

    private void navigateToLeaderboard() {
        if (isAdded()) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new LeaderboardFragment())
                    .commit();
        }
    }
}