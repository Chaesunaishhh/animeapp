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

    EditText email, password;
    Button loginBtn;
    TextView goToRegister;
    ImageView togglePassword;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        // 🔥 AUTO LOGIN
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        email = findViewById(R.id.emailInput);
        password = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginButton);
        goToRegister = findViewById(R.id.goToRegister);
        togglePassword = findViewById(R.id.togglePassword);

        loginBtn.setOnClickListener(v -> loginUser());

        goToRegister.setOnClickListener(v -> {
            new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("Confirm Registration")
                    .setMessage("Are you sure you want to sign up for a new account?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        togglePassword.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void loginUser() {
        String e = email.getText().toString().trim();
        String p = password.getText().toString().trim();

        if (TextUtils.isEmpty(e)) {
            email.setError("Email required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            email.setError("Enter valid email");
            return;
        }

        if (TextUtils.isEmpty(p)) {
            password.setError("Password required");
            return;
        }

        setLoading(true);

        auth.signInWithEmailAndPassword(e, p)
                .addOnSuccessListener(result -> {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                    goToMain();
                })
                .addOnFailureListener(err -> {
                    Toast.makeText(this,
                            "Invalid email or password. Please try again.",
                            Toast.LENGTH_SHORT).show();
                    setLoading(false);
                });

    }

    private void setLoading(boolean isLoading) {
        loginBtn.setEnabled(!isLoading);
        loginBtn.setText(isLoading ? "Logging in..." : "Login");
    }

    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void togglePasswordVisibility() {
        if (password.getInputType() == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            password.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            togglePassword.setImageResource(R.drawable.ic_eye_off);
        } else {
            password.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            togglePassword.setImageResource(R.drawable.ic_eye);
        }
        password.setSelection(password.getText().length());
    }
}
