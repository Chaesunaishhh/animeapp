package com.jeff.animeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText email, password, username;
    Button registerBtn;
    TextView goToLogin;
    ImageView togglePassword, registerProfileImage;
    private android.net.Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseStorage storage;
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        username = findViewById(R.id.usernameInput);
        email = findViewById(R.id.emailInput);
        password = findViewById(R.id.passwordInput);
        registerBtn = findViewById(R.id.registerButton);
        goToLogin = findViewById(R.id.goToLogin);
        togglePassword = findViewById(R.id.togglePassword);
        registerProfileImage = findViewById(R.id.registerProfileImage);
    }

    private void setupClickListeners() {
        registerBtn.setOnClickListener(v -> registerUser());
        goToLogin.setOnClickListener(v -> goToLoginActivity());
        togglePassword.setOnClickListener(v -> togglePasswordVisibility());
        findViewById(R.id.btnSelectProfilePic).setOnClickListener(v -> openImagePicker());
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            registerProfileImage.setImageURI(selectedImageUri);
            registerProfileImage.setPadding(0, 0, 0, 0); // Remove padding when image is set
        }
    }

    private void registerUser() {
        if (isProcessing) return;

        if (!validateInputs()) return;

        String usernameStr = username.getText().toString().trim();
        String emailStr = email.getText().toString().trim();
        String passwordStr = password.getText().toString().trim();

        isProcessing = true;
        setLoading(true);

        auth.createUserWithEmailAndPassword(emailStr, passwordStr)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            saveUserProfile(user.getUid(), usernameStr, emailStr);
                        } else {
                            handleRegistrationComplete();
                        }
                    } else {
                        // Registration failed
                        handleRegistrationError(task.getException());
                    }
                });
    }

    private void saveUserProfile(String uid, String usernameStr, String emailStr) {
        if (selectedImageUri != null) {
            uploadProfileImage(uid, usernameStr, emailStr);
        } else {
            saveUserToFirestore(uid, usernameStr, emailStr, "");
        }
    }

    private void uploadProfileImage(String uid, String usernameStr, String emailStr) {
        StorageReference storageRef = storage.getReference().child("profile_images/" + uid + ".jpg");

        // Add metadata to help the server identify the file type
        com.google.firebase.storage.StorageMetadata metadata = new com.google.firebase.storage.StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

        storageRef.putFile(selectedImageUri, metadata)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveUserToFirestore(uid, usernameStr, emailStr, uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("StorageError", "Upload failed", e);
                    // Even if upload fails, we might want to save the user data without the image
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    saveUserToFirestore(uid, usernameStr, emailStr, "");
                });
    }

    private void saveUserToFirestore(String uid, String usernameStr, String emailStr, String profileImageUrl) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", usernameStr);
        userMap.put("email", emailStr);
        userMap.put("profileImage", profileImageUrl);
        userMap.put("watchedCount", 0);
        userMap.put("watchlistCount", 0);
        userMap.put("quizCount", 0);
        userMap.put("quizAvgScore", 0.0);
        userMap.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(uid).set(userMap)
                .addOnCompleteListener(task -> {
                    handleRegistrationComplete();
                });
    }

    private void handleRegistrationComplete() {
        setLoading(false);
        isProcessing = false;

        // ✅ RESET QUIZ DATA FOR NEW USER
        SharedPreferences prefs = getSharedPreferences("QuizData", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String usernameStr = username.getText().toString().trim();

        editor.remove("last_" + usernameStr);
        editor.remove("total_" + usernameStr);
        editor.remove("quiz_week_" + usernameStr);
        editor.remove("answers_" + usernameStr);
        editor.remove("questions_" + usernameStr);

        editor.apply();

        Toast.makeText(this,
                "Account created successfully!\nPlease login with your credentials.",
                Toast.LENGTH_LONG).show();

        new android.os.Handler().postDelayed(() -> {
            goToLoginActivity();
        }, 1500);
    }

    private void handleRegistrationError(Exception error) {
        setLoading(false);
        isProcessing = false;
        Toast.makeText(this, "❌ " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private boolean validateInputs() {
        String usernameStr = username.getText().toString().trim();
        String emailStr = email.getText().toString().trim();
        String passwordStr = password.getText().toString().trim();

        if (TextUtils.isEmpty(usernameStr)) {
            username.setError("Username required");
            return false;
        }
        if (TextUtils.isEmpty(emailStr)) {
            email.setError("Email required");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            email.setError("Enter valid email");
            return false;
        }
        if (TextUtils.isEmpty(passwordStr)) {
            password.setError("Password required");
            return false;
        }
        if (passwordStr.length() < 6) {
            password.setError("Password must be at least 6 characters");
            return false;
        }
        return true;
    }

    private void setLoading(boolean isLoading) {
        registerBtn.setEnabled(!isLoading);
        registerBtn.setText(isLoading ? "Creating..." : "Create Account");
    }

    private void goToLoginActivity() {
        auth.signOut();

        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
    }

    private void togglePasswordVisibility() {
        int currentInputType = password.getInputType();
        if (currentInputType == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            password.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            togglePassword.setImageResource(R.drawable.ic_eye_off);
        } else {
            password.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            togglePassword.setImageResource(R.drawable.ic_eye);
        }
        password.setSelection(password.getText().length());
    }

}