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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private Button loginBtn;
    private TextView goToRegister;
    private ImageView togglePassword;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupClickListeners();
        checkAutoLogin();
    }

    private void initViews() {
        auth = FirebaseAuth.getInstance();
        email = findViewById(R.id.emailInput);
        password = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginButton);
        goToRegister = findViewById(R.id.goToRegister);
        togglePassword = findViewById(R.id.togglePassword);
    }

    private void setupClickListeners() {
        loginBtn.setOnClickListener(v -> loginUser());
        goToRegister.setOnClickListener(v -> showRegisterDialog());
        togglePassword.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void checkAutoLogin() {
        if (auth.getCurrentUser() != null) {
            goToMainActivity();
        }
    }

    private void loginUser() {
        if (!validateInputs()) return;

        String emailStr = email.getText().toString().trim();
        String passwordStr = password.getText().toString().trim();

        setLoading(true);

        auth.signInWithEmailAndPassword(emailStr, passwordStr)
                .addOnSuccessListener(result -> {
                    setLoading(false);
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                    goToMainActivity();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateInputs() {
        String emailStr = email.getText().toString().trim();
        String passwordStr = password.getText().toString().trim();

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
        return true;
    }

    private void showRegisterDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Create New Account")
                .setMessage("Don't have an account? Sign up now!")
                .setPositiveButton("Sign Up", (dialog, which) -> {
                    startActivity(new Intent(this, RegisterActivity.class));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setLoading(boolean isLoading) {
        loginBtn.setEnabled(!isLoading);
        loginBtn.setText(isLoading ? "Logging in..." : "Login");
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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