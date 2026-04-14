package com.jeff.animeapp.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.*;
import androidx.fragment.app.Fragment;


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
        Button btnLeaderboard = v.findViewById(R.id.btnLeaderboard);


        SharedPreferences session = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String username = session.getString("logged_in_user", "");


        if (username.isEmpty()) {
            showMessage("No user logged in!", 0x33FF9800);
            setupLeaderboardButton(btnLeaderboard);
            return v;
        }


        SharedPreferences prefs = requireActivity().getSharedPreferences("QuizData", Context.MODE_PRIVATE);


        // ✅ CHECK IF USER TOOK QUIZ THIS WEEK
        long currentWeekStart = getWeekStart();
        long lastQuizWeek = prefs.getLong("quiz_week_" + username, -1);


        if (lastQuizWeek != currentWeekStart) {
            // User hasn't taken quiz this week
            showMessage("You haven't taken this week's quiz yet!\nTake the quiz first to see your review.", 0x33FF9800);
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


        // Add header to show it's this week's quiz
        TextView header = new TextView(getContext());
        header.setText("📝 THIS WEEK'S QUIZ RESULTS 📝\n\n");
        header.setTextSize(18);
        header.setPadding(20, 20, 20, 10);
        header.setTextColor(0xFFFFFFFF);
        header.setGravity(Gravity.CENTER);
        container.addView(header);


        // Loop safely through user answers
        int correctCount = 0;


        for (int i = 0; i < userAnswers.size() && i < qIndex.size(); i++) {


            int idx = qIndex.get(i);


            // Safety check to prevent crash
            if (idx >= questions.length || idx >= answers.length) continue;


            String userAns = userAnswers.get(i);
            String correct = answers[idx];


            if (userAns.equals(correct)) {
                correctCount++;
            }


            TextView tv = new TextView(getContext());


            String result = "❓ Q" + (i + 1) + ": " + questions[idx] +
                    "\n📝 Your Answer: " + userAns +
                    "\n✅ Correct Answer: " + correct;


            tv.setText(result);
            tv.setTextSize(16);
            tv.setPadding(20, 20, 20, 20);
            tv.setTextColor(0xFFFFFFFF);


            if (userAns.equals(correct)) {
                tv.setBackgroundColor(0x334CAF50); // Green for correct
            } else {
                tv.setBackgroundColor(0x33F44336); // Red for wrong
            }


            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 20);


            tv.setLayoutParams(params);
            container.addView(tv);
        }


        // Add score summary at the bottom
        TextView summary = new TextView(getContext());
        int totalQuestions = userAnswers.size();
        int score = correctCount * 10;
        summary.setText("\n📊 SUMMARY: " + correctCount + "/" + totalQuestions + " correct\n⭐ Score: " + score + " points\n");
        summary.setTextSize(16);
        summary.setPadding(20, 20, 20, 20);
        summary.setTextColor(0xFFFFFFFF);
        summary.setBackgroundColor(0x332196F3);
        summary.setGravity(Gravity.CENTER);
        container.addView(summary);


        setupLeaderboardButton(btnLeaderboard);
        return v;
    }


    private void showMessage(String message, int backgroundColor) {
        TextView messageView = new TextView(getContext());
        messageView.setText(message);
        messageView.setTextSize(18);
        messageView.setPadding(40, 40, 40, 40);
        messageView.setTextColor(0xFFFFFFFF);
        messageView.setBackgroundColor(backgroundColor);
        messageView.setGravity(Gravity.CENTER);
        container.addView(messageView);
    }


    private void setupLeaderboardButton(Button btnLeaderboard) {
        btnLeaderboard.setOnClickListener(view -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new LeaderboardFragment())
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
