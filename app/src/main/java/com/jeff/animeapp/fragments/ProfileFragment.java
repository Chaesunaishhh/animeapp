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
import com.jeff.animeapp.R;
import com.jeff.animeapp.LoginActivity;
import com.jeff.animeapp.notifications.NotificationHelper;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class ProfileFragment extends Fragment {

    private Button logoutBtn;
    private TextView usernameView, emailView, statWatchedView, tvInWatchlist, tvQuizzesTaken, tvQuizAvgScore;
    private ImageView profileImage;
    private TextView progressCollector, progressMasterCollector, progressFinisher, progressMasterFinisher, progressLegendaryOtaku, progressQuizEnthusiast;
    private ImageView imgCollector, imgMasterCollector, imgFinisher, imgMasterFinisher, imgLegendary, imgQuiz;
    private ImageView iconCollector, iconMasterCollector, iconFinisher, iconMasterFinisher, iconLegendary, iconQuiz;
    private View glowCollector, glowMasterCollector, glowFinisher, glowMasterFinisher, glowLegendary, glowQuiz;
    private TextView txtCollectorVal, txtMasterCollectorVal, txtFinisherVal, txtMasterFinisherVal, txtLegendaryVal, txtQuizVal;
    private SwitchCompat switchDarkMode;
    private View btnNotifications, btnHelpSupport, btnEditProfileButton;
    
    private Uri selectedImageUri;
    private ImageView editProfileImgView;
    private android.widget.ProgressBar editProgressBar;
    private AlertDialog editDialog;

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    if (editProfileImgView != null) {
                        Glide.with(this).load(uri).into(editProfileImgView);
                    }
                }
            }
    );

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        logoutBtn = v.findViewById(R.id.logoutButton);
        usernameView = v.findViewById(R.id.profileUsername);
        emailView = v.findViewById(R.id.profileEmail);
        profileImage = v.findViewById(R.id.profileImage);
        statWatchedView = v.findViewById(R.id.statWatched);
        tvInWatchlist = v.findViewById(R.id.tvInWatchlist);
        tvQuizzesTaken = v.findViewById(R.id.tvQuizzesTaken);
        tvQuizAvgScore = v.findViewById(R.id.tvQuizAvgScore);

        // Achievement Images
        imgCollector = v.findViewById(R.id.imgCollector);
        imgMasterCollector = v.findViewById(R.id.imgMasterCollector);
        imgFinisher = v.findViewById(R.id.imgFinisher);
        imgMasterFinisher = v.findViewById(R.id.imgMasterFinisher);
        imgLegendary = v.findViewById(R.id.imgLegendary);
        imgQuiz = v.findViewById(R.id.imgQuiz);

        // Achievement Icons
        iconCollector = v.findViewById(R.id.iconCollector);
        iconMasterCollector = v.findViewById(R.id.iconMasterCollector);
        iconFinisher = v.findViewById(R.id.iconFinisher);
        iconMasterFinisher = v.findViewById(R.id.iconMasterFinisher);
        iconLegendary = v.findViewById(R.id.iconLegendary);
        iconQuiz = v.findViewById(R.id.iconQuiz);

        // Achievement Glows
        glowCollector = v.findViewById(R.id.glowCollector);
        glowMasterCollector = v.findViewById(R.id.glowMasterCollector);
        glowFinisher = v.findViewById(R.id.glowFinisher);
        glowMasterFinisher = v.findViewById(R.id.glowMasterFinisher);
        glowLegendary = v.findViewById(R.id.glowLegendary);
        glowQuiz = v.findViewById(R.id.glowQuiz);

        // Achievement Text Values
        txtCollectorVal = v.findViewById(R.id.txtCollectorVal);
        txtMasterCollectorVal = v.findViewById(R.id.txtMasterCollectorVal);
        txtFinisherVal = v.findViewById(R.id.txtFinisherVal);
        txtMasterFinisherVal = v.findViewById(R.id.txtMasterFinisherVal);
        txtLegendaryVal = v.findViewById(R.id.txtLegendaryVal);
        txtQuizVal = v.findViewById(R.id.txtQuizVal);

        // Progress TextViews
        progressCollector = v.findViewById(R.id.progressCollector);
        progressMasterCollector = v.findViewById(R.id.progressMasterCollector);
        progressFinisher = v.findViewById(R.id.progressFinisher);
        progressMasterFinisher = v.findViewById(R.id.progressMasterFinisher);
        progressLegendaryOtaku = v.findViewById(R.id.progressLegendaryOtaku);
        progressQuizEnthusiast = v.findViewById(R.id.progressQuizEnthusiast);

        // Settings
        switchDarkMode = v.findViewById(R.id.switchDarkMode);
        btnNotifications = v.findViewById(R.id.btnNotifications);
        btnHelpSupport = v.findViewById(R.id.btnHelpSupport);
        btnEditProfileButton = v.findViewById(R.id.btnEditProfileRow);

        setupSettings();

        btnEditProfileButton.setOnClickListener(v1 -> {
            showEditProfileDialog();
        });

        logoutBtn.setOnClickListener(view -> {
            new AlertDialog.Builder(getContext(), R.style.AnimeAlertDialog)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        performLogout();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        loadUserData();

        return v;
    }

    private void setupSettings() {
        // SharedPreferences
        SharedPreferences prefs = getActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("DarkMode", true);
        switchDarkMode.setChecked(isDark);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("DarkMode", isChecked);
            editor.apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            Toast.makeText(getContext(), "Theme updated!", Toast.LENGTH_SHORT).show();
        });

        btnNotifications.setOnClickListener(v -> {
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

        btnHelpSupport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(android.net.Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@animeapp.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "App Support - " + (usernameView != null ? usernameView.getText().toString() : "User"));
            try {
                startActivity(Intent.createChooser(intent, "Send Email"));
            } catch (Exception e) {
                Toast.makeText(getContext(), "No email client found", Toast.LENGTH_SHORT).show();
            }
        });

        if (btnEditProfileButton != null) {
            btnEditProfileButton.setOnClickListener(v -> showEditProfileDialog());
        }
    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_profile, null);
        EditText editUsername = dialogView.findViewById(R.id.editUsername);
        EditText editEmail = dialogView.findViewById(R.id.editEmail);
        EditText editPassword = dialogView.findViewById(R.id.editPassword);
        editProfileImgView = dialogView.findViewById(R.id.editProfileImage);
        View btnChangePic = dialogView.findViewById(R.id.btnChangeProfilePic);

        selectedImageUri = null; // Reset for new edit session

        String currentUsername = usernameView.getText().toString();
        String currentEmail = emailView.getText().toString();

        editUsername.setText(currentUsername);
        editEmail.setText(currentEmail);

        // Show current image in dialog
        if (profileImage.getDrawable() != null) {
            editProfileImgView.setImageDrawable(profileImage.getDrawable());
            editProfileImgView.setPadding(0, 0, 0, 0);
        }

        btnChangePic.setOnClickListener(v -> {
            mGetContent.launch("image/*");
        });

        editProgressBar = dialogView.findViewById(R.id.editProfileProgressBar);

        editDialog = new AlertDialog.Builder(requireContext(), R.style.AnimeAlertDialog)
                .setView(dialogView)
                .setPositiveButton("Save Changes", null) // Set listener later to control dismiss
                .setNegativeButton("Cancel", (dialog, which) -> {
                    editProfileImgView = null;
                })
                .create();

        editDialog.show();

        editDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newName = editUsername.getText().toString().trim();
            String newEmail = editEmail.getText().toString().trim();
            String newPass = editPassword.getText().toString().trim();

            if (!newName.isEmpty()) {
                updateProfile(newName, newEmail, newPass, currentUsername, currentEmail);
            }
        });
    }

    private void updateProfile(String newName, String newEmail, String newPass, String oldName, String oldEmail) {
        com.google.firebase.auth.FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (editProgressBar != null) editProgressBar.setVisibility(View.VISIBLE);

        // Track tasks to know when to dismiss
        final boolean[] imgDone = {selectedImageUri == null};
        final boolean[] nameDone = {newName.equals(oldName)};
        final boolean[] emailDone = {newEmail.equals(oldEmail) || newEmail.isEmpty()};
        final boolean[] passDone = {newPass.isEmpty()};

        Runnable checkDismiss = () -> {
            if (imgDone[0] && nameDone[0] && emailDone[0] && passDone[0]) {
                if (editProgressBar != null) editProgressBar.setVisibility(View.GONE);
                if (editDialog != null && editDialog.isShowing()) editDialog.dismiss();
            }
        };

        // 0. Update Profile Picture if selected
        if (selectedImageUri != null) {
            uploadImage(selectedImageUri, () -> {
                imgDone[0] = true;
                checkDismiss.run();
            });
        }

        // 1. Update Firestore Username (if changed)
        if (!newName.equals(oldName)) {
            db.collection("users").document(uid)
                    .update("username", newName)
                    .addOnSuccessListener(aVoid -> {
                        usernameView.setText(newName);
                        NotificationHelper.sendNotification(getContext(), "Profile Updated", "Your username has been changed to " + newName);
                        nameDone[0] = true;
                        checkDismiss.run();
                    })
                    .addOnFailureListener(e -> {
                        nameDone[0] = true;
                        checkDismiss.run();
                    });
        }

        // 2. Update Auth Email (if changed)
        if (!newEmail.equals(oldEmail) && !newEmail.isEmpty()) {
            user.updateEmail(newEmail)
                    .addOnSuccessListener(aVoid -> {
                        emailView.setText(newEmail);
                        db.collection("users").document(uid).update("email", newEmail);
                        Toast.makeText(getContext(), "Email updated!", Toast.LENGTH_SHORT).show();
                        emailDone[0] = true;
                        checkDismiss.run();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Email update failed. Re-login required.", Toast.LENGTH_LONG).show();
                        emailDone[0] = true;
                        checkDismiss.run();
                    });
        }

        // 3. Update Auth Password (if provided)
        if (!newPass.isEmpty()) {
            if (newPass.length() < 6) {
                Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                passDone[0] = true;
                checkDismiss.run();
            } else {
                user.updatePassword(newPass)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Password updated!", Toast.LENGTH_SHORT).show();
                            passDone[0] = true;
                            checkDismiss.run();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Password update failed. Re-login required.", Toast.LENGTH_LONG).show();
                            passDone[0] = true;
                            checkDismiss.run();
                        });
            }
        }
        
        // Final check in case nothing was changed
        checkDismiss.run();
    }

    private void uploadImage(Uri imageUri, Runnable onComplete) {
        com.google.firebase.auth.FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        
        String uid = user.getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_images/" + uid + ".jpg");

        // Add metadata to help the server identify the file type
        com.google.firebase.storage.StorageMetadata metadata = new com.google.firebase.storage.StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

        storageRef.putFile(imageUri, metadata)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        FirebaseFirestore.getInstance().collection("users").document(uid)
                                .update("profileImage", downloadUrl)
                                .addOnSuccessListener(aVoid -> {
                                    if (isAdded()) {
                                        Glide.with(this).load(downloadUrl).into(profileImage);
                                        Toast.makeText(getContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
                                    }
                                    if (onComplete != null) onComplete.run();
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    if (onComplete != null) onComplete.run();
                });
    }

    private void loadUserData() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener((DocumentSnapshot doc) -> {
                        if (doc.exists() && isAdded()) {
                            String username = doc.getString("username");
                            String email = doc.getString("email");
                            Long watchedCount = doc.getLong("watchedCount");
                            Long watchlistCount = doc.getLong("watchlistCount");
                            Long quizCount = doc.getLong("quizCount");
                            Double quizAvgScore = doc.getDouble("quizAvgScore");

                            if (username != null) usernameView.setText(username);
                            if (email != null) emailView.setText(email);
                            else if (auth.getCurrentUser().getEmail() != null) emailView.setText(auth.getCurrentUser().getEmail());

                            String profilePicUrl = doc.getString("profileImage");
                            if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                                Glide.with(getContext())
                                        .load(profilePicUrl)
                                        .placeholder(R.drawable.ic_profile_placeholder)
                                        .into(profileImage);
                            } else {
                                profileImage.setImageResource(R.drawable.ic_profile_placeholder);
                            }

                            // Statistics
                            statWatchedView.setText(String.valueOf(watchedCount != null ? watchedCount : 0));
                            tvInWatchlist.setText(String.valueOf(watchlistCount != null ? watchlistCount : 0));
                            tvQuizzesTaken.setText(String.valueOf(quizCount != null ? quizCount : 0));

                            if (quizAvgScore != null) {
                                int avg = (int) Math.round(quizAvgScore);
                                tvQuizAvgScore.setText(avg + "%");
                            } else {
                                tvQuizAvgScore.setText("0%");
                            }

                            updateAchievements(watchedCount, watchlistCount, quizCount);
                        }
                    });
        }
    }

    private void updateAchievements(Long watchedCount, Long watchlistCount, Long quizCount) {
        if (watchlistCount == null) watchlistCount = 0L;
        if (watchedCount == null) watchedCount = 0L;
        if (quizCount == null) quizCount = 0L;

        // Collector (10)
        updateBadge(imgCollector, iconCollector, glowCollector, txtCollectorVal, progressCollector, watchlistCount, 10);
        // Master Collector (30)
        updateBadge(imgMasterCollector, iconMasterCollector, glowMasterCollector, txtMasterCollectorVal, progressMasterCollector, watchlistCount, 30);
        // Finisher (5)
        updateBadge(imgFinisher, iconFinisher, glowFinisher, txtFinisherVal, progressFinisher, watchedCount, 5);
        // Master Finisher (20)
        updateBadge(imgMasterFinisher, iconMasterFinisher, glowMasterFinisher, txtMasterFinisherVal, progressMasterFinisher, watchedCount, 20);
        // Legendary (50)
        updateBadge(imgLegendary, iconLegendary, glowLegendary, txtLegendaryVal, progressLegendaryOtaku, watchedCount, 50);
        // Quizzer (10)
        updateBadge(imgQuiz, iconQuiz, glowQuiz, txtQuizVal, progressQuizEnthusiast, quizCount, 10);
    }

    private void updateBadge(ImageView img, ImageView icon, View glow, TextView val, TextView prog, long current, int target) {
        if (current >= target) {
            img.setImageResource(R.drawable.bg_hexagon);
            img.setImageTintList(null); 
            icon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FFD700"))); // Gold
            glow.setVisibility(View.VISIBLE);
            
            // Grand glow animation
            glow.setAlpha(0f);
            glow.animate().alpha(0.8f).setDuration(1500).setStartDelay(200).start();
            
            val.setTextColor(Color.WHITE);
            val.setAlpha(1.0f);
            
            prog.setText("UNLOCKED");
            prog.setTextColor(Color.parseColor("#F72585")); // Pink accent
            prog.setTypeface(null, android.graphics.Typeface.BOLD);
            
            // Scale icon up a bit for grand look
            icon.setScaleX(1.2f);
            icon.setScaleY(1.2f);
        } else {
            img.setImageResource(R.drawable.bg_hexagon_locked);
            img.setImageTintList(null);
            icon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#33FFFFFF")));
            glow.setVisibility(View.GONE);
            val.setTextColor(Color.parseColor("#33FFFFFF"));
            val.setAlpha(0.5f);
            prog.setText(current + " / " + target);
            prog.setTextColor(Color.parseColor("#66FFFFFF"));
            prog.setTypeface(null, android.graphics.Typeface.NORMAL);
            
            icon.setScaleX(1.0f);
            icon.setScaleY(1.0f);
        }
    }

    private void performLogout() {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }
}
