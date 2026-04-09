package com.jeff.animeapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jeff.animeapp.R;
import com.jeff.animeapp.LoginActivity;

public class ProfileFragment extends Fragment {

    private Button logoutBtn;
    private TextView usernameView, emailView, statWatchedView;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        logoutBtn = v.findViewById(R.id.logoutButton);
        usernameView = v.findViewById(R.id.profileUsername);
        emailView = v.findViewById(R.id.profileEmail);
        statWatchedView = v.findViewById(R.id.statWatched);

        logoutBtn.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
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
                            Long watchedCount = doc.getLong("watchedCount");

                            if (username != null) usernameView.setText(username);
                            if (email != null) emailView.setText(email);
                            if (watchedCount != null) {
                                statWatchedView.setText(watchedCount + " Anime Watched");
                            } else {
                                statWatchedView.setText("0 Anime Watched");
                            }
                        }
                    });
        }

        return v;
    }
}
