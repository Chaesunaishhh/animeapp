package com.jeff.animeapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.jeff.animeapp.R;

import java.util.*;

public class QuizFragment extends Fragment {

    private TextView tvQuestion, tvScore, tvQuestionCount, tvLives;
    private ProgressBar progressBar;
    private final Button[] options = new Button[4];

    private int score = 0;
    private int currentIdx = 0;
    private int lives = 3;
    private String currentUsername;
    private boolean isAnswering = false;

    private List<Integer> questionIndices;
    private List<String> userAnswers = new ArrayList<>();
    private long currentWeekStart;

    private final int COLOR_DEFAULT = Color.parseColor("#1E1E2C");
    private final int COLOR_CORRECT = Color.parseColor("#4CAF50");
    private final int COLOR_WRONG = Color.parseColor("#F44336");

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

        SharedPreferences session = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUsername = session.getString("logged_in_user", null);

        if (currentUsername == null) {
            Toast.makeText(getContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
            return inflater.inflate(R.layout.fragment_quiz, container, false);
        }

        View v = inflater.inflate(R.layout.fragment_quiz, container, false);

        tvQuestion = v.findViewById(R.id.tvQuestion);
        tvScore = v.findViewById(R.id.tvScore);
        tvQuestionCount = v.findViewById(R.id.tvQuestionCount);
        tvLives = v.findViewById(R.id.tvLives);
        progressBar = v.findViewById(R.id.quizProgress);

        options[0] = v.findViewById(R.id.btnOption1);
        options[1] = v.findViewById(R.id.btnOption2);
        options[2] = v.findViewById(R.id.btnOption3);
        options[3] = v.findViewById(R.id.btnOption4);

        // Reset userAnswers for new quiz session
        userAnswers = new ArrayList<>();

        // Weekly check
        if (!canUserTakeQuizThisWeek()) {
            new Handler(Looper.getMainLooper()).post(this::openReview);
            return v;
        }

        setupRandomQuestions();
        progressBar.setMax(questionIndices.size());

        updateUI();

        for (Button btn : options) {
            btn.setOnClickListener(view -> checkAnswer((Button) view));
        }

        return v;
    }

    private boolean canUserTakeQuizThisWeek() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("QuizData", Context.MODE_PRIVATE);
        currentWeekStart = getWeekStart();
        long last;
        try {
            last = prefs.getLong("quiz_week_" + currentUsername, -1);
        } catch (ClassCastException e) {
            // If the value was incorrectly stored as a String, clear it
            prefs.edit().remove("quiz_week_" + currentUsername).apply();
            last = -1;
        }
        return last != currentWeekStart;
    }

    private long getWeekStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private void setupRandomQuestions() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < questions.length; i++) list.add(i);
        Collections.shuffle(list);
        questionIndices = list.subList(0, Math.min(10, list.size()));
    }

    private void updateUI() {
        isAnswering = false;

        int idx = questionIndices.get(currentIdx);

        tvScore.setText("Score: " + score);
        tvQuestionCount.setText((currentIdx + 1) + "/" + questionIndices.size());
        tvLives.setText("❤️".repeat(lives));

        progressBar.setProgress(currentIdx + 1);

        for (Button b : options) {
            b.setEnabled(true);
            b.setBackgroundTintList(ColorStateList.valueOf(COLOR_DEFAULT));
        }

        tvQuestion.setText(questions[idx]);

        List<String> shuffled = new ArrayList<>(Arrays.asList(choices[idx]));
        Collections.shuffle(shuffled);

        for (int i = 0; i < 4; i++) {
            options[i].setText(shuffled.get(i));
        }
    }

    private void checkAnswer(Button btn) {
        if (isAnswering) return;
        isAnswering = true;

        int idx = questionIndices.get(currentIdx);
        String correct = answers[idx];

        userAnswers.add(btn.getText().toString());

        for (Button b : options) b.setEnabled(false);

        if (btn.getText().toString().equals(correct)) {
            btn.setBackgroundTintList(ColorStateList.valueOf(COLOR_CORRECT));
            score += 10;
        } else {
            btn.setBackgroundTintList(ColorStateList.valueOf(COLOR_WRONG));
            lives--;

            for (Button b : options) {
                if (b.getText().toString().equals(correct)) {
                    b.setBackgroundTintList(ColorStateList.valueOf(COLOR_CORRECT));
                }
            }
        }

        new Handler(Looper.getMainLooper()).postDelayed(this::nextQuestion, 1200);
    }

    private void nextQuestion() {
        if (lives <= 0) {
            finishQuiz();
            return;
        }

        currentIdx++;

        if (currentIdx >= questionIndices.size()) {
            finishQuiz();
        } else {
            updateUI();
        }
    }

    private void finishQuiz() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("QuizData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("quiz_week_" + currentUsername, currentWeekStart);
        editor.putInt("last_" + currentUsername, score);

        int total = prefs.getInt("total_" + currentUsername, 0) + score;
        editor.putInt("total_" + currentUsername, total);

        // ✅ SAVE USER ANSWERS AND QUESTION INDICES FOR REVIEW
        Gson gson = new Gson();
        String answersJson = gson.toJson(userAnswers);
        String questionsJson = gson.toJson(questionIndices);

        editor.putString("answers_" + currentUsername, answersJson);
        editor.putString("questions_" + currentUsername, questionsJson);

        editor.apply();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("username", currentUsername);
        data.put("weeklyScore", score);
        data.put("totalScore", total);
        data.put("week", currentWeekStart);

        db.collection("weekly_leaderboard")
                .document(currentUsername)
                .set(data);

        openReview();
    }

    private void openReview() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new QuizReviewFragment())
                .commit();
    }
}