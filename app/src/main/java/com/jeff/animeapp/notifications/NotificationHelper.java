package com.jeff.animeapp.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.view.View;
import android.widget.TextView;
import com.jeff.animeapp.MainActivity;
import com.jeff.animeapp.R;

import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jeff.animeapp.fragments.NotificationsFragment;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationHelper {

    private static final String CHANNEL_ID = "anime_quiz_notifications";
    private static final String CHANNEL_NAME = "Quiz Notifications";
    private static final String CHANNEL_DESC = "Notifications for quiz results and updates";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void sendNotification(Context context, String title, String message) {
        // 0. Save Notification for In-App List
        saveNotificationLocally(context, title, message);

        // 1. Show In-App Notification if MainActivity is accessible
        MainActivity mainActivity = null;
        if (context instanceof MainActivity) {
            mainActivity = (MainActivity) context;
        } else if (context instanceof android.view.ContextThemeWrapper) {
            Context baseContext = ((android.view.ContextThemeWrapper) context).getBaseContext();
            if (baseContext instanceof MainActivity) {
                mainActivity = (MainActivity) baseContext;
            }
        }

        if (mainActivity != null) {
            showInAppNotification(mainActivity, title, message);
        }

        // 2. System Notification (Always send as fallback/background)
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException e) {
            // Permission not granted for POST_NOTIFICATIONS
        }
    }

    private static void saveNotificationLocally(Context context, String title, String message) {
        SharedPreferences prefs = context.getSharedPreferences("AppNotifications", Context.MODE_PRIVATE);
        String json = prefs.getString("notifications_list", null);
        Gson gson = new Gson();
        Type type = new TypeToken<List<NotificationsFragment.NotificationItem>>() {}.getType();
        List<NotificationsFragment.NotificationItem> list;

        if (json == null) {
            list = new ArrayList<>();
        } else {
            list = gson.fromJson(json, type);
        }

        String time = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(new Date());
        list.add(new NotificationsFragment.NotificationItem(title, message, time));

        prefs.edit().putString("notifications_list", gson.toJson(list)).apply();
    }

    private static void showInAppNotification(MainActivity activity, String title, String message) {
        activity.runOnUiThread(() -> {
            View notifView = activity.findViewById(R.id.inAppNotification);
            if (notifView == null) return;

            TextView tvTitle = notifView.findViewById(R.id.notifTitle);
            TextView tvMsg = notifView.findViewById(R.id.notifMessage);

            tvTitle.setText(title);
            tvMsg.setText(message);

            notifView.setVisibility(View.VISIBLE);
            
            // Set click listener to open notifications page
            notifView.setOnClickListener(v -> {
                activity.getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragmentContainer, new NotificationsFragment())
                        .addToBackStack(null)
                        .commit();
                notifView.setVisibility(View.GONE);
            });

            notifView.setTranslationY(-300f);
            
            notifView.animate()
                    .translationY(0f)
                    .setDuration(500)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .withEndAction(() -> {
                        notifView.postDelayed(() -> {
                            if (notifView.getWindowToken() != null) {
                                notifView.animate()
                                        .translationY(-300f)
                                        .setDuration(500)
                                        .setInterpolator(new android.view.animation.AccelerateInterpolator())
                                        .withEndAction(() -> notifView.setVisibility(View.GONE))
                                        .start();
                            }
                        }, 3500);
                    })
                    .start();
        });
    }
}
