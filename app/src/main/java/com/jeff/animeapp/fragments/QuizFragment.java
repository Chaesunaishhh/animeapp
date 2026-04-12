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
import com.jeff.animeapp.R;

public class QuizFragment extends Fragment {

    private TextView tvQuestion, tvScore, tvQuestionCount;
    private Button[] options = new Button[4];
    private int score = 0;
    private int currentIdx = 0;

    private String[] questions = {
            "Who is the protagonist of 'Attack on Titan'?",
            "What is Goku's signature move?",
            "Who is the 'Copy Ninja' in Naruto?",
            "Luffy's Devil Fruit is the...?",
            "What is the Shinigami's name in 'Death Note'?",
            "Which anime features Nezuko?",
            "Who is the Fullmetal Alchemist?",
            "What is Saitama's hero name?",
            "Who is the King of Curses in JJK?",
            "The world of Sword Art Online is called...?"
    };

    private String[][] choices = {
            {"Levi", "Eren Yeager", "Mikasa", "Armin"},
            {"Chidori", "Rasengan", "Kamehameha", "Bankai"},
            {"Kakashi", "Sasuke", "Itachi", "Minato"},
            {"Gum-Gum", "Flame-Flame", "Ice-Ice", "Dark-Dark"},
            {"Ryuk", "Rem", "Light", "L"},
            {"Black Clover", "Demon Slayer", "Bleach", "Fate"},
            {"Alphonse", "Edward Elric", "Mustang", "Scar"},
            {"Caped Baldy", "One Punch Man", "Silver Fang", "Genos"},
            {"Gojo", "Sukuna", "Mahito", "Jogo"},
            {"Aincrad", "Alfheim", "Gun Gale", "Underworld"}
    };

    private String[] answers = {
            "Eren Yeager", "Kamehameha", "Kakashi", "Gum-Gum", "Ryuk",
            "Demon Slayer", "Edward Elric", "Caped Baldy", "Sukuna", "Aincrad"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_quiz, container, false);

        tvQuestion = v.findViewById(R.id.tvQuestion);
        tvScore = v.findViewById(R.id.tvScore);
        tvQuestionCount = v.findViewById(R.id.tvQuestionCount);
        options[0] = v.findViewById(R.id.btnOption1);
        options[1] = v.findViewById(R.id.btnOption2);
        options[2] = v.findViewById(R.id.btnOption3);
        options[3] = v.findViewById(R.id.btnOption4);

        updateUI();

        for (Button btn : options) {
            btn.setOnClickListener(view -> {
                String selected = ((Button)view).getText().toString();

                // Feedback Logic
                if (selected.equals(answers[currentIdx])) {
                    score += 10;
                    tvScore.setText("Score: " + score);
                    Toast.makeText(getContext(), "Correct!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Wrong! Answer: " + answers[currentIdx], Toast.LENGTH_SHORT).show();
                }

                if (currentIdx < 9) {
                    currentIdx++;
                    updateUI();
                } else {
                    saveAndNavigate();
                }
            });
        }
        return v;
    }

    private void updateUI() {
        tvQuestionCount.setText("Question " + (currentIdx + 1) + " of 10");
        tvQuestion.setText(questions[currentIdx]);
        for (int i = 0; i < 4; i++) options[i].setText(choices[currentIdx][i]);
    }

    private void saveAndNavigate() {
        // Record score in SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("QuizData", Context.MODE_PRIVATE);
        int totalPoints = prefs.getInt("total_score", 0);

        prefs.edit()
                .putInt("last_score", score)
                .putInt("total_score", totalPoints + score)
                .apply();

        // Go to Leaderboard
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new LeaderboardFragment())
                .commit();
    }
}