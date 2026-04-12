package com.jeff.animeapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
    private boolean isAnswering = false;
    private List<Integer> questionIndices;
    private long currentWeekStart; // For weekly tracking

    // UI Colors
    private final int COLOR_DEFAULT_GRAY = Color.parseColor("#1E1E2C");
    private final int COLOR_CORRECT_GREEN = Color.parseColor("#4CAF50");
    private final int COLOR_WRONG_RED = Color.parseColor("#F44336");

    // Full 30-Question Bank
    private final String[] questions = {
            "Who is the protagonist of 'Attack on Titan'?", "What is Goku's signature move?",
            "Who is the 'Copy Ninja' in Naruto?", "Luffy's Devil Fruit is the...?",
            "What is the Shinigami's name in 'Death Note'?", "Which anime features Nezuko?",
            "Who is the Fullmetal Alchemist?", "What is Saitama's hero name?",
            "Who is the King of Curses in JJK?", "The world of Sword Art Online is called...?",
            "What is the name of the fox spirit inside Naruto?", "In 'One Piece', who is the doctor of the Straw Hat Pirates?",
            "What is the name of the captain of the Seven Deadly Sins?", "Who is the 'Symbol of Peace' in My Hero Academia?",
            "What is the name of Spike Spiegel's ship in Cowboy Bebop?", "In 'Bleach', what is the name of Ichigo's sword?",
            "Who is known as the 'Elric Brothers' mother?", "What is the name of the virtual world in Digimon?",
            "Which anime features a notebook that can kill people?", "Who is the 'Black Swordsman' in Berserk?",
            "What is the name of the dragon in 'Dragon Ball'?", "In 'Hunter x Hunter', what is Killua's last name?",
            "Who is the captain of the 10th Division in Bleach?", "What is the name of the cat in 'Sailor Moon'?",
            "In 'Fairy Tail', what type of magic does Natsu use?", "Who is the creator of the 'Blue Lock' project?",
            "What is the main power system in 'JoJo's Bizarre Adventure'?", "Who is the 'Flame Hashira' in Demon Slayer?",
            "What is the name of the orphanage in 'The Promised Neverland'?", "Which anime involves 'Quirks'?"
    };

    private final String[][] choices = {
            {"Levi", "Eren Yeager", "Mikasa", "Armin"}, {"Chidori", "Rasengan", "Kamehameha", "Bankai"},
            {"Kakashi", "Sasuke", "Itachi", "Minato"}, {"Gum-Gum", "Flame-Flame", "Ice-Ice", "Dark-Dark"},
            {"Ryuk", "Rem", "Light", "L"}, {"Black Clover", "Demon Slayer", "Bleach", "Fate"},
            {"Alphonse", "Edward Elric", "Mustang", "Scar"}, {"Caped Baldy", "One Punch Man", "Silver Fang", "Genos"},
            {"Gojo", "Sukuna", "Mahito", "Jogo"}, {"Aincrad", "Alfheim", "Gun Gale", "Underworld"},
            {"Kurama", "Shukaku", "Matatabi", "Gyuki"}, {"Zoro", "Nami", "Chopper", "Sanji"},
            {"Ban", "Meliodas", "King", "Escanor"}, {"Endeavor", "All Might", "Bakugo", "Deku"},
            {"Bebop", "Swordfish", "Red Tail", "Hammerhead"}, {"Zangetsu", "Senbonzakura", "Tensa", "Muramasa"},
            {"Trisha", "Winry", "Izumi", "Pinako"}, {"Digital World", "Cyber Space", "Net World", "Metaverse"},
            {"Death Note", "Code Geass", "Monster", "Psycho-Pass"}, {"Guts", "Griffith", "Casca", "Judeau"},
            {"Shenron", "Porunga", "Rayquaza", "Tiamat"}, {"Zoldyck", "Freecss", "Kurapika", "Lucilfer"},
            {"Hitsugaya", "Byakuya", "Kenpachi", "Aizen"}, {"Luna", "Artemis", "Diana", "Meowth"},
            {"Fire Dragon Slayer", "Ice Make", "Celestial", "Requip"}, {"Ego Jinpachi", "Anri Teieri", "Isagi", "Kira"},
            {"Stands", "Nen", "Chakra", "Ki"}, {"Rengoku", "Tengen", "Shinobu", "Giyu"},
            {"Grace Field", "Glory Bell", "Grand Valley", "Goldy Pond"}, {"MHA", "Naruto", "One Piece", "Black Clover"}
    };

    private final String[] answers = {
            "Eren Yeager", "Kamehameha", "Kakashi", "Gum-Gum", "Ryuk",
            "Demon Slayer", "Edward Elric", "Caped Baldy", "Sukuna", "Aincrad",
            "Kurama", "Chopper", "Meliodas", "All Might", "Bebop", "Zangetsu",
            "Trisha", "Digital World", "Death Note", "Guts", "Shenron", "Zoldyck",
            "Hitsugaya", "Luna", "Fire Dragon Slayer", "Ego Jinpachi", "Stands", "Rengoku",
            "Grace Field", "MHA"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SharedPreferences userSession = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUsername = userSession.getString("logged_in_user", "Guest");

        // **CHECK ONE-TAKE-PER-WEEK RULE FOR THIS USER ONLY**
        if (!canUserTakeQuizThisWeek()) {
            Toast.makeText(getContext(), " You can only take the quiz ONCE per week!", Toast.LENGTH_LONG).show();
            if (isAdded()) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new LeaderboardFragment())
                        .commit();
            }
            return null; // Don't create the quiz view
        }

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
            btn.setOnClickListener(view -> checkAnswer((Button) view));
        }

        return v;
    }

    /**
     * Check if current user can take quiz this week
     * Each user has their own weekly tracking
     */
    private boolean canUserTakeQuizThisWeek() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("QuizData", Context.MODE_PRIVATE);
        long lastQuizTime = prefs.getLong(currentUsername + "_last_quiz_week_start", 0);
        currentWeekStart = getCurrentWeekStartTimestamp();

        // If no previous quiz OR different week, allow quiz
        return lastQuizTime == 0 || lastQuizTime != currentWeekStart;
    }

    /**
     * Get timestamp for start of current week (Monday 00:00)
     */
    private long getCurrentWeekStartTimestamp() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private void setupRandomQuestions() {
        // **WEEKLY ROTATING SET: Use week number to seed random selection**
        Random rng = new Random(currentWeekStart); // Same seed = same 10 questions every week
        questionIndices = new ArrayList<>();

        // Select exactly 10 unique questions based on weekly seed
        List<Integer> allIndices = new ArrayList<>();
        for (int i = 0; i < questions.length; i++) allIndices.add(i);

        Collections.shuffle(allIndices, rng); // Weekly consistent shuffle
        for (int i = 0; i < 10; i++) {
            questionIndices.add(allIndices.get(i));
        }
    }

    private void updateUI() {
        if (currentIdx >= 10) return;

        isAnswering = false;
        int actualIdx = questionIndices.get(currentIdx);

        tvScore.setText("Score: " + score);
        tvQuestionCount.setText("Question " + (currentIdx + 1) + " of 10");
        progressBar.setProgress(currentIdx + 1);

        for (Button btn : options) {
            btn.setBackgroundTintList(ColorStateList.valueOf(COLOR_DEFAULT_GRAY));
            btn.setEnabled(true);
        }

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
        Collections.addAll(currentChoices, choices[actualIdx]);
        Collections.shuffle(currentChoices);

        for (int i = 0; i < 4; i++) options[i].setText(currentChoices.get(i));
    }

    private void checkAnswer(Button selectedBtn) {
        if (isAnswering) return;
        isAnswering = true;

        int actualIdx = questionIndices.get(currentIdx);
        String selectedText = selectedBtn.getText().toString();
        String correctText = answers[actualIdx];

        for (Button btn : options) btn.setEnabled(false);

        if (selectedText.equals(correctText)) {
            selectedBtn.setBackgroundTintList(ColorStateList.valueOf(COLOR_CORRECT_GREEN));
            score += isBonus ? 20 : 10;
            new Handler(Looper.getMainLooper()).postDelayed(this::nextQuestion, 600);
        } else {
            selectedBtn.setBackgroundTintList(ColorStateList.valueOf(COLOR_WRONG_RED));
            for (Button btn : options) {
                if (btn.getText().toString().equals(correctText)) {
                    btn.setBackgroundTintList(ColorStateList.valueOf(COLOR_CORRECT_GREEN));
                }
            }
            lives--;
            new Handler(Looper.getMainLooper()).postDelayed(this::nextQuestion, 1500);
        }
    }

    private void nextQuestion() {
        currentIdx++;
        if (lives <= 0 || currentIdx >= 10) {
            saveAndNavigate();
        } else {
            updateUI();
        }
    }

    private void saveAndNavigate() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("QuizData", Context.MODE_PRIVATE);
        int currentTotal = prefs.getInt(currentUsername + "_total_score", 0);
        int newTotal = currentTotal + score;

        // **MARK THIS USER as having taken quiz this week**
        prefs.edit()
                .putInt(currentUsername + "_last_score", score)
                .putInt(currentUsername + "_total_score", newTotal)
                .putLong(currentUsername + "_last_quiz_week_start", currentWeekStart) // KEY: Per-user weekly lock
                .apply();

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