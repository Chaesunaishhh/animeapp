package com.jeff.animeapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.jeff.animeapp.R;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    Button loginBtn;
    TextView goToRegister;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        // 🔥 AUTO LOGIN (if already logged in, skip login screen)
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        setContentView(R.layout.activity_login);

        email = findViewById(R.id.emailInput);
        password = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginButton);
        goToRegister = findViewById(R.id.goToRegister);

        loginBtn.setOnClickListener(v -> loginUser());

        goToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void loginUser() {
        String e = email.getText().toString().trim();
        String p = password.getText().toString().trim();

        // ✅ Validation
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
                    Toast.makeText(this, err.getMessage(), Toast.LENGTH_SHORT).show();
                    setLoading(false);
                });
    }

    // 🔥 Handle loading state cleanly
    private void setLoading(boolean isLoading) {
        loginBtn.setEnabled(!isLoading);
        loginBtn.setText(isLoading ? "Logging in..." : "Login");
    }

    // 🔥 Clean navigation method
    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}