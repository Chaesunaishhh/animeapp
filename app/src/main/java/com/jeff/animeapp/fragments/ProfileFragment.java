package com.jeff.animeapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jeff.animeapp.R;
import com.jeff.animeapp.LoginActivity;

public class ProfileFragment extends Fragment {

    private Button logoutBtn;
    private Switch darkModeSwitch;
    private TextView usernameView, emailView;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        logoutBtn = v.findViewById(R.id.logoutButton);
        darkModeSwitch = v.findViewById(R.id.darkModeSwitch);
        usernameView = v.findViewById(R.id.profileUsername);
        emailView = v.findViewById(R.id.profileEmail);

        logoutBtn.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        });

        int currentNightMode = AppCompatDelegate.getDefaultNightMode();
        darkModeSwitch.setChecked(currentNightMode == AppCompatDelegate.MODE_NIGHT_YES);

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener((DocumentSnapshot doc) -> {
                        if (doc.exists()) {
                            String username = doc.getString("username");
                            String email = doc.getString("email");

                            if (username != null) usernameView.setText(username);
                            if (email != null) emailView.setText(email);
                        }
                    });
        }

        return v;
    }
}
