package com.jeff.animeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.jeff.animeapp.fragments.CommunityFragment;
import com.jeff.animeapp.fragments.HomeFragment;
import com.jeff.animeapp.fragments.ProfileFragment;
import com.jeff.animeapp.fragments.WatchlistFragment;
import com.jeff.animeapp.notifications.NotificationHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 🔥 APPLY THEME BEFORE SETCONTENTVIEW
        android.content.SharedPreferences prefs = getSharedPreferences("Settings", android.content.Context.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("DarkMode", true);
        if (isDark) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);

        NotificationHelper.createNotificationChannel(this);
        requestNotificationPermission();

        // 🔥 LOGIN CHECK FIRST
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNavigation);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_watchlist) {
                selectedFragment = new WatchlistFragment();
            } else if (id == R.id.nav_community) {
                selectedFragment = new CommunityFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
            }

            return true;
        });

        // Default fragment
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) 
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, 
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    // 🔑 FILTER DIALOG FUNCTION
    public void showFilterDialog(HomeFragment homeFragment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AnimeAlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.filter_dialog, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button btnApply = dialogView.findViewById(R.id.btnApply);
        Button btnReset = dialogView.findViewById(R.id.btnReset);

        // Genre checkboxes
        CheckBox cbAction = dialogView.findViewById(R.id.cbAction);
        CheckBox cbComedy = dialogView.findViewById(R.id.cbComedy);
        CheckBox cbRomance = dialogView.findViewById(R.id.cbRomance);
        CheckBox cbFantasy = dialogView.findViewById(R.id.cbFantasy);

        btnApply.setOnClickListener(a -> {
            List<String> selectedGenres = new ArrayList<>();
            if (cbAction.isChecked()) selectedGenres.add("Action");
            if (cbComedy.isChecked()) selectedGenres.add("Comedy");
            if (cbRomance.isChecked()) selectedGenres.add("Romance");
            if (cbFantasy.isChecked()) selectedGenres.add("Fantasy");

            List<String> selectedYears = new ArrayList<>();
            // Loop through 2000–2026
            for (int year = 2000; year <= 2026; year++) {
                int resId = dialogView.getResources()
                        .getIdentifier("cb" + year, "id", dialogView.getContext().getPackageName());
                CheckBox cbYear = dialogView.findViewById(resId);
                if (cbYear != null && cbYear.isChecked()) {
                    selectedYears.add(String.valueOf(year));
                }
            }

            // Call HomeFragment filter function
            homeFragment.applyFilters(selectedGenres, selectedYears);

            dialog.dismiss();
        });

        btnReset.setOnClickListener(r -> {
            // Clear genres
            cbAction.setChecked(false);
            cbComedy.setChecked(false);
            cbRomance.setChecked(false);
            cbFantasy.setChecked(false);

            // Clear years 2000–2026
            for (int year = 2000; year <= 2026; year++) {
                int resId = dialogView.getResources()
                        .getIdentifier("cb" + year, "id", dialogView.getContext().getPackageName());
                CheckBox cbYear = dialogView.findViewById(resId);
                if (cbYear != null) cbYear.setChecked(false);
            }

            // Reset list in HomeFragment
            homeFragment.applyFilters(new ArrayList<>(), new ArrayList<>());
            dialog.dismiss();
        });
    }
}
