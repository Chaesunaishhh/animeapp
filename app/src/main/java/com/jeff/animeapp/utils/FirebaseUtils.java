package com.jeff.animeapp.utils;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseUtils {

    // Get FirebaseAuth instance
    public static FirebaseAuth auth() {
        return FirebaseAuth.getInstance();
    }

    // Get Firestore instance
    public static FirebaseFirestore firestore() {
        return FirebaseFirestore.getInstance();
    }

    // Get current user UID safely (nullable if not logged in)
    @Nullable
    public static String uid() {
        if (auth().getCurrentUser() != null) {
            return auth().getCurrentUser().getUid();
        } else {
            return null; // No user logged in
        }
    }

    // Helper to check if user is logged in
    public static boolean isLoggedIn() {
        return auth().getCurrentUser() != null;
    }
}