package com.jeff.animeapp.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.jeff.animeapp.R;
import com.jeff.animeapp.LoginActivity;
import com.jeff.animeapp.notifications.NotificationHelper;
import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private Button logoutBtn;
    private TextView usernameView, emailView, statWatchedView, tvInWatchlist, tvQuizzesTaken, tvQuizAvgScore;
    private TextView progressCollector, progressMasterCollector, progressFinisher, progressMasterFinisher, progressLegendaryOtaku, progressQuizEnthusiast;
    private ImageView imgCollector, imgMasterCollector, imgFinisher, imgMasterFinisher, imgLegendary, imgQuiz;
    private ImageView iconCollector, iconMasterCollector, iconFinisher, iconMasterFinisher, iconLegendary, iconQuiz;
    private View glowCollector, glowMasterCollector, glowFinisher, glowMasterFinisher, glowLegendary, glowQuiz;
    private TextView txtCollectorVal, txtMasterCollectorVal, txtFinisherVal, txtMasterFinisherVal, txtLegendaryVal, txtQuizVal;
    private SwitchCompat switchDarkMode;
    private View btnNotifications, btnHelpSupport, btnEditProfileButton;
    private android.widget.ProgressBar profileProgressBar;
    
    private android.widget.ProgressBar editProgressBar;
    private AlertDialog editDialog;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(v);
        setupSettings(v);
        loadUserData();

        return v;
    }

    private void initViews(View v) {
        logoutBtn = v.findViewById(R.id.logoutButton);
        usernameView = v.findViewById(R.id.profileUsername);
        emailView = v.findViewById(R.id.profileEmail);
        statWatchedView = v.findViewById(R.id.statWatched);
        tvInWatchlist = v.findViewById(R.id.tvInWatchlist);
        tvQuizzesTaken = v.findViewById(R.id.tvQuizzesTaken);
        tvQuizAvgScore = v.findViewById(R.id.tvQuizAvgScore);
        profileProgressBar = v.findViewById(R.id.profileProgressBar);
        // Views initialized in setupBadgeViews
        setupBadgeViews(v);

        switchDarkMode = v.findViewById(R.id.switchDarkMode);

        View rowNotif = v.findViewById(R.id.itemNotif);
        btnNotifications = rowNotif.findViewById(R.id.menuRow);
        ((ImageView)rowNotif.findViewById(R.id.menuIcon)).setImageResource(R.drawable.ic_notifications);
        ((TextView)rowNotif.findViewById(R.id.menuTitle)).setText("Notifications");

        View rowHelp = v.findViewById(R.id.itemHelp);
        btnHelpSupport = rowHelp.findViewById(R.id.menuRow);
        ((ImageView)rowHelp.findViewById(R.id.menuIcon)).setImageResource(R.drawable.ic_community);
        ((TextView)rowHelp.findViewById(R.id.menuTitle)).setText("Help & Support");

        btnEditProfileButton = v.findViewById(R.id.btnEditProfileRow);

        logoutBtn.setOnClickListener(view -> performLogout());
        btnEditProfileButton.setOnClickListener(view -> showEditProfileDialog());
    }

    private void setupBadgeViews(View v) {
        View b1 = v.findViewById(R.id.badge1);
        imgCollector = b1.findViewById(R.id.achievementBadge);
        iconCollector = b1.findViewById(R.id.achievementIcon);
        glowCollector = b1.findViewById(R.id.glowEffect);
        txtCollectorVal = b1.findViewById(R.id.achievementValue);
        progressCollector = b1.findViewById(R.id.achievementProgress);
        ((TextView)b1.findViewById(R.id.achievementTitle)).setText("COLLECTOR");

        View b2 = v.findViewById(R.id.badge2);
        imgMasterCollector = b2.findViewById(R.id.achievementBadge);
        iconMasterCollector = b2.findViewById(R.id.achievementIcon);
        glowMasterCollector = b2.findViewById(R.id.glowEffect);
        txtMasterCollectorVal = b2.findViewById(R.id.achievementValue);
        progressMasterCollector = b2.findViewById(R.id.achievementProgress);
        ((TextView)b2.findViewById(R.id.achievementTitle)).setText("MASTER");

        View b3 = v.findViewById(R.id.badge3);
        imgFinisher = b3.findViewById(R.id.achievementBadge);
        iconFinisher = b3.findViewById(R.id.achievementIcon);
        glowFinisher = b3.findViewById(R.id.glowEffect);
        txtFinisherVal = b3.findViewById(R.id.achievementValue);
        progressFinisher = b3.findViewById(R.id.achievementProgress);
        ((TextView)b3.findViewById(R.id.achievementTitle)).setText("FINISHER");

        View b4 = v.findViewById(R.id.badge4);
        imgMasterFinisher = b4.findViewById(R.id.achievementBadge);
        iconMasterFinisher = b4.findViewById(R.id.achievementIcon);
        glowMasterFinisher = b4.findViewById(R.id.glowEffect);
        txtMasterFinisherVal = b4.findViewById(R.id.achievementValue);
        progressMasterFinisher = b4.findViewById(R.id.achievementProgress);
        ((TextView)b4.findViewById(R.id.achievementTitle)).setText("ELITE");

        View b5 = v.findViewById(R.id.badge5);
        imgLegendary = b5.findViewById(R.id.achievementBadge);
        iconLegendary = b5.findViewById(R.id.achievementIcon);
        glowLegendary = b5.findViewById(R.id.glowEffect);
        txtLegendaryVal = b5.findViewById(R.id.achievementValue);
        progressLegendaryOtaku = b5.findViewById(R.id.achievementProgress);
        ((TextView)b5.findViewById(R.id.achievementTitle)).setText("LEGEND");

        View b6 = v.findViewById(R.id.badge6);
        imgQuiz = b6.findViewById(R.id.achievementBadge);
        iconQuiz = b6.findViewById(R.id.achievementIcon);
        glowQuiz = b6.findViewById(R.id.glowEffect);
        txtQuizVal = b6.findViewById(R.id.achievementValue);
        progressQuizEnthusiast = b6.findViewById(R.id.achievementProgress);
        ((TextView)b6.findViewById(R.id.achievementTitle)).setText("QUIZZER");
    }

    private void setupSettings(View root) {
        SharedPreferences prefs = getActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("DarkMode", true);
        switchDarkMode.setChecked(isDark);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("DarkMode", isChecked);
            editor.apply();

            AppCompatDelegate.setDefaultNightMode(isChecked ? 
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            Toast.makeText(getContext(), "Theme updated!", Toast.LENGTH_SHORT).show();
        });

        View itemNotif = root.findViewById(R.id.itemNotif);
        if (itemNotif != null) {
            ((TextView)itemNotif.findViewById(R.id.menuTitle)).setText("Notifications");
            ((ImageView)itemNotif.findViewById(R.id.menuIcon)).setImageResource(R.drawable.ic_notifications);
            itemNotif.setOnClickListener(v -> {
                Intent intent = new Intent();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    intent.setAction(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
                } else {
                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                    intent.putExtra("app_package", requireContext().getPackageName());
                    intent.putExtra("app_uid", requireContext().getApplicationInfo().uid);
                }
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Could not open settings", Toast.LENGTH_SHORT).show();
                }
            });
        }

        View itemHelp = root.findViewById(R.id.itemHelp);
        if (itemHelp != null) {
            ((TextView)itemHelp.findViewById(R.id.menuTitle)).setText("Help & Support");
            ((ImageView)itemHelp.findViewById(R.id.menuIcon)).setImageResource(R.drawable.ic_quiz);
            itemHelp.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@animeapp.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "App Support - " + (usernameView != null ? usernameView.getText().toString() : "User"));
                try {
                    startActivity(Intent.createChooser(intent, "Send Email"));
                } catch (Exception e) {
                    Toast.makeText(getContext(), "No email client found", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnEditProfileButton != null) {
            btnEditProfileButton.setOnClickListener(v -> showEditProfileDialog());
        }

        logoutBtn.setOnClickListener(view -> {
            new AlertDialog.Builder(requireContext(), R.style.AnimeAlertDialog)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> performLogout())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_profile, null);
        EditText editUsername = dialogView.findViewById(R.id.editUsername);
        EditText editEmail = dialogView.findViewById(R.id.editEmail);

        editUsername.setText(usernameView.getText());
        editEmail.setText(emailView.getText());

        editProgressBar = dialogView.findViewById(R.id.editProfileProgressBar);

        editDialog = new AlertDialog.Builder(requireContext(), R.style.AnimeAlertDialog)
                .setView(dialogView)
                .setPositiveButton("Save Changes", null)
                .setNegativeButton("Cancel", null)
                .create();

        editDialog.show();
        editDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newName = editUsername.getText().toString().trim();
            String newEmail = editEmail.getText().toString().trim();
            String newPass = ((EditText)dialogView.findViewById(R.id.editPassword)).getText().toString().trim();
            
            if (newName.isEmpty()) {
                editUsername.setError("Username required");
                return;
            }
            
            updateProfile(newName, newEmail, newPass);
        });
    }

    private void updateProfile(String newName, String newEmail, String newPass) {
        com.google.firebase.auth.FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        if (editProgressBar != null) editProgressBar.setVisibility(View.VISIBLE);

        // Handle Email Change
        if (!newEmail.equals(user.getEmail()) && !newEmail.isEmpty()) {
            user.updateEmail(newEmail).addOnFailureListener(e -> 
                Toast.makeText(getContext(), "Failed to update email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        // Handle Password Change
        if (!newPass.isEmpty() && newPass.length() >= 6) {
            user.updatePassword(newPass).addOnFailureListener(e -> 
                Toast.makeText(getContext(), "Failed to update password: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        finalizeProfileUpdate(user.getUid(), newName, newEmail);
    }

    private void finalizeProfileUpdate(String uid, String newName, String newEmail) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", newName);
        updates.put("email", newEmail);

        FirebaseFirestore.getInstance().collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        usernameView.setText(newName);
                        emailView.setText(newEmail);
                        if (editProgressBar != null) editProgressBar.setVisibility(View.GONE);
                        if (editDialog != null) editDialog.dismiss();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        if (editProgressBar != null) editProgressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserData() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            if (profileProgressBar != null) profileProgressBar.setVisibility(View.VISIBLE);
            
            FirebaseFirestore.getInstance().collection("users").document(auth.getCurrentUser().getUid())
                    .get().addOnSuccessListener(doc -> {
                        if (profileProgressBar != null) profileProgressBar.setVisibility(View.GONE);

                        if (doc.exists() && isAdded()) {
                            usernameView.setText(doc.getString("username"));
                            emailView.setText(doc.getString("email"));

                            Long watched = doc.getLong("watchedCount");
                            Long list = doc.getLong("watchlistCount");
                            Long quiz = doc.getLong("quizCount");
                            Double avg = doc.getDouble("quizAvgScore");

                            statWatchedView.setText(String.valueOf(watched != null ? watched : 0));
                            tvInWatchlist.setText(String.valueOf(list != null ? list : 0));
                            tvQuizzesTaken.setText(String.valueOf(quiz != null ? quiz : 0));
                            tvQuizAvgScore.setText((avg != null ? Math.round(avg) : 0) + "%");

                            updateAchievements(watched, list, quiz);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (profileProgressBar != null) profileProgressBar.setVisibility(View.GONE);
                    });
        }
    }

    private void updateAchievements(Long watched, Long list, Long quiz) {
        updateBadge(imgCollector, iconCollector, glowCollector, txtCollectorVal, progressCollector, list != null ? list : 0, 10);
        updateBadge(imgMasterCollector, iconMasterCollector, glowMasterCollector, txtMasterCollectorVal, progressMasterCollector, list != null ? list : 0, 30);
        updateBadge(imgFinisher, iconFinisher, glowFinisher, txtFinisherVal, progressFinisher, watched != null ? watched : 0, 5);
        updateBadge(imgMasterFinisher, iconMasterFinisher, glowMasterFinisher, txtMasterFinisherVal, progressMasterFinisher, watched != null ? watched : 0, 20);
        updateBadge(imgLegendary, iconLegendary, glowLegendary, txtLegendaryVal, progressLegendaryOtaku, watched != null ? watched : 0, 50);
        updateBadge(imgQuiz, iconQuiz, glowQuiz, txtQuizVal, progressQuizEnthusiast, quiz != null ? quiz : 0, 10);
    }

    private void updateBadge(ImageView img, ImageView icon, View glow, TextView val, TextView prog, long current, int target) {
        if (current >= target) {
            img.setImageResource(R.drawable.bg_hexagon);
            icon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FFD700")));
            glow.setVisibility(View.VISIBLE);
            glow.animate().alpha(0.8f).setDuration(1500).start();
            prog.setText("UNLOCKED");
            prog.setTextColor(Color.parseColor("#F72585"));
        } else {
            img.setImageResource(R.drawable.bg_hexagon_locked);
            icon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#33FFFFFF")));
            glow.setVisibility(View.GONE);
            prog.setText(current + " / " + target);
        }
    }

    private void performLogout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }
}