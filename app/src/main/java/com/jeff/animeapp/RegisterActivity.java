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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        username = findViewById(R.id.usernameInput);
        email = findViewById(R.id.emailInput);
        password = findViewById(R.id.passwordInput);
        registerBtn = findViewById(R.id.registerButton);
        goToLogin = findViewById(R.id.goToLogin);
        togglePassword = findViewById(R.id.togglePassword);

        registerBtn.setOnClickListener(v -> registerUser());

        goToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        togglePassword.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void registerUser() {
        String user = username.getText().toString().trim();
        String e = email.getText().toString().trim();
        String p = password.getText().toString().trim();

        if (TextUtils.isEmpty(user)) {
            username.setError("Username required");
            return;
        }

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

        if (p.length() < 6) {
            password.setError("Password must be at least 6 characters");
            return;
        }

        registerBtn.setEnabled(false);
        registerBtn.setText("Creating account...");

//        auth.createUserWithEmailAndPassword(e, p)
//                .addOnSuccessListener(result -> {
//                    String uid = auth.getCurrentUser().getUid();
//
//                    Map<String, Object> userMap = new HashMap<>();
//                    userMap.put("username", user);
//                    userMap.put("email", e);
//                    userMap.put("createdAt", System.currentTimeMillis());
//
//                    db.collection("users").document(uid).set(userMap);
//
//                    Toast.makeText(RegisterActivity.this, "Account created successfully. Please log in.", Toast.LENGTH_LONG).show();
//
//                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
//                    finish();
//                })
//                .addOnFailureListener(err -> {
//                    Toast.makeText(RegisterActivity.this, err.getMessage(), Toast.LENGTH_SHORT).show();
//                    registerBtn.setEnabled(true);
//                    registerBtn.setText("Create Account");
//                });
        auth.createUserWithEmailAndPassword(e, p)
                .addOnSuccessListener(result -> {
                    String uid = auth.getCurrentUser().getUid();

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("username", user);
                    userMap.put("email", e);
                    userMap.put("createdAt", System.currentTimeMillis());

                    db.collection("users").document(uid).set(userMap);

                    // ✅ Always redirect back to login with clear toast
                    Toast.makeText(RegisterActivity.this,
                            "Account created successfully. Please log in.",
                            Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(err -> {
                    Toast.makeText(RegisterActivity.this, err.getMessage(), Toast.LENGTH_SHORT).show();
                    registerBtn.setEnabled(true);
                    registerBtn.setText("Create Account");
                });

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
