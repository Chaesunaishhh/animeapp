package com.jeff.animeapp;

import android.content.Intent;
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

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText email, password, username;
    Button registerBtn;
    TextView goToLogin;
    ImageView togglePassword;
    FirebaseAuth auth;
    FirebaseFirestore db;
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

        username = findViewById(R.id.usernameInput);
        email = findViewById(R.id.emailInput);
        password = findViewById(R.id.passwordInput);
        registerBtn = findViewById(R.id.registerButton);
        goToLogin = findViewById(R.id.goToLogin);
        togglePassword = findViewById(R.id.togglePassword);
    }

    private void setupClickListeners() {
        registerBtn.setOnClickListener(v -> registerUser());
        goToLogin.setOnClickListener(v -> goToLoginActivity());
        togglePassword.setOnClickListener(v -> togglePasswordVisibility());
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
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", usernameStr);
        userMap.put("email", emailStr);
        userMap.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(uid).set(userMap)
                .addOnCompleteListener(task -> {

                    handleRegistrationComplete();
                });
    }

    private void handleRegistrationComplete() {
        setLoading(false);
        isProcessing = false;

        Toast.makeText(this,
                "Account created successfully!\nPlease login with your credentials.",
                Toast.LENGTH_LONG).show();

        new android.os.Handler().postDelayed(() -> {
            goToLoginActivity();
        }, 1500); // 1.5 second delay to show toast
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