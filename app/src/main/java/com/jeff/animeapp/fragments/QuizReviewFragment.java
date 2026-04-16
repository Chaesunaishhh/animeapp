package com.jeff.animeapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.*;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jeff.animeapp.R;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.List;

public class QuizReviewFragment extends Fragment {

    private LinearLayout container;

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

    private final String[] answers = {
            "Eren Yeager", "Kamehameha", "Kakashi", "Gum-Gum", "Ryuk",
            "Demon Slayer", "Edward Elric", "Caped Baldy", "Sukuna", "Aincrad",
            "Kurama", "Chopper", "Meliodas", "All Might", "Bebop", "Zangetsu",
            "Trisha", "Digital World", "Death Note", "Guts", "Shenron", "Zoldyck",
            "Hitsugaya", "Luna", "Fire Dragon Slayer", "Ego Jinpachi", "Stands",
            "Rengoku", "Grace Field", "MHA"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_review, parent, false);

        container = v.findViewById(R.id.reviewContainer);
        MaterialButton btnLeaderboard = v.findViewById(R.id.btnLeaderboard);
        View btnBack = v.findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(view -> getParentFragmentManager().popBackStack());
        }

        SharedPreferences session = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String username = session.getString("logged_in_user", "");

        if (username.isEmpty()) {
            showMessage("No user logged in!", 0x33F72585);
            setupLeaderboardButton(btnLeaderboard);
            return v;
        }

        SharedPreferences prefs = requireActivity().getSharedPreferences("QuizData", Context.MODE_PRIVATE);

        // ✅ CHECK IF USER TOOK QUIZ THIS WEEK
        long currentWeekStart = getWeekStart();
        long lastQuizWeek;
        try {
            lastQuizWeek = prefs.getLong("quiz_week_" + username, -1);
        } catch (ClassCastException e) {
            // If the value was incorrectly stored as a String, clear it
            prefs.edit().remove("quiz_week_" + username).apply();
            lastQuizWeek = -1;
        }

        if (lastQuizWeek != currentWeekStart) {
            // User hasn't taken quiz this week
            showMessage("You haven't taken this week's quiz yet!\nTake the quiz first to see your review.", 0x33F72585);
            setupLeaderboardButton(btnLeaderboard);
            return v;
        }

        Gson gson = new Gson();

        Type typeA = new TypeToken<List<String>>(){}.getType();
        String answersJson = prefs.getString("answers_" + username, "[]");
        List<String> userAnswers = gson.fromJson(answersJson, typeA);

        Type typeQ = new TypeToken<List<Integer>>(){}.getType();
        String questionsJson = prefs.getString("questions_" + username, "[]");
        List<Integer> qIndex = gson.fromJson(questionsJson, typeQ);

        // Check if there's any data to show
        if (userAnswers.isEmpty() || qIndex.isEmpty()) {
            showMessage("No quiz data found. Please take the quiz first!", 0x33F44336);
            setupLeaderboardButton(btnLeaderboard);
            return v;
        }

        int correctCount = 0;
        for (int i = 0; i < userAnswers.size() && i < qIndex.size(); i++) {

            int idx = qIndex.get(i);

            // Safety check to prevent crash
            if (idx >= questions.length || idx >= answers.length) continue;

            String userAns = userAnswers.get(i);
            String correct = answers[idx];

            boolean isCorrect = userAns.equals(correct);
            if (isCorrect) {
                correctCount++;
            }

            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_quiz_review, container, false);
            TextView tvQuestion = itemView.findViewById(R.id.tvQuestion);
            TextView tvUserAnswer = itemView.findViewById(R.id.tvUserAnswer);
            TextView tvCorrectAnswer = itemView.findViewById(R.id.tvCorrectAnswer);
            MaterialCardView card = itemView.findViewById(R.id.cardReview);

            tvQuestion.setText("Q" + (i + 1) + ": " + questions[idx]);
            tvUserAnswer.setText(userAns);
            tvCorrectAnswer.setText(correct);

            if (isCorrect) {
                card.setStrokeColor(0xFF4CAF50);
                card.setCardBackgroundColor(0x114CAF50);
                tvUserAnswer.setTextColor(0xFF4CAF50);
            } else {
                card.setStrokeColor(0xFFF72585);
                card.setCardBackgroundColor(0x11F72585);
                tvUserAnswer.setTextColor(0xFFF72585);
            }

            container.addView(itemView);
        }

        // Add score summary at the top
        int totalQuestions = userAnswers.size();
        int score = correctCount * 10;

        View summaryView = LayoutInflater.from(getContext()).inflate(R.layout.item_quiz_summary, container, false);
        TextView tvScore = summaryView.findViewById(R.id.tvTotalScore);
        TextView tvCorrect = summaryView.findViewById(R.id.tvCorrectCount);
        TextView tvMessage = summaryView.findViewById(R.id.tvScoreMessage);

        tvScore.setText(String.valueOf(score));
        tvCorrect.setText(correctCount + " / " + totalQuestions);

        if (correctCount == totalQuestions) tvMessage.setText("Perfect Score! You're a True Otaku!");
        else if (correctCount >= totalQuestions * 0.7) tvMessage.setText("Great job! Almost there!");
        else if (correctCount >= totalQuestions * 0.4) tvMessage.setText("Not bad, keep watching!");
        else tvMessage.setText("Time to watch more anime!");

        container.addView(summaryView, 0);

        // Header for questions
        TextView qHeader = new TextView(getContext());
        qHeader.setText("Detailed Review");
        qHeader.setTextSize(18);
        qHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        qHeader.setTextColor(0xFFFFFFFF);
        qHeader.setPadding(0, 32, 0, 16);
        container.addView(qHeader, 1);

        setupLeaderboardButton(btnLeaderboard);
        return v;
    }

    private void showMessage(String message, int backgroundColor) {
        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 32);
        card.setLayoutParams(params);
        card.setRadius(24);
        card.setCardBackgroundColor(backgroundColor);
        card.setStrokeWidth(0);

        TextView messageView = new TextView(getContext());
        messageView.setText(message);
        messageView.setTextSize(16);
        messageView.setPadding(48, 48, 48, 48);
        messageView.setTextColor(0xFFFFFFFF);
        messageView.setGravity(Gravity.CENTER);
        
        card.addView(messageView);
        container.addView(card);
    }

    private void setupLeaderboardButton(MaterialButton btnLeaderboard) {
        btnLeaderboard.setOnClickListener(view -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new LeaderboardFragment())
                    .addToBackStack(null)
                    .commit();
        });
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
}