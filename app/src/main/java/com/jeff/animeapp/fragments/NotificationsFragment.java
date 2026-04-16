package com.jeff.animeapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jeff.animeapp.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList = new ArrayList<>();
    private LinearLayout emptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = v.findViewById(R.id.recyclerNotifications);
        emptyState = v.findViewById(R.id.emptyState);
        
        v.findViewById(R.id.btnBack).setOnClickListener(view -> getParentFragmentManager().popBackStack());
        
        v.findViewById(R.id.btnMarkRead).setOnClickListener(view -> {
            markAllAsRead();
        });

        setupRecyclerView();
        loadNotifications();

        return v;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);
    }

    private void loadNotifications() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppNotifications", Context.MODE_PRIVATE);
        String json = prefs.getString("notifications_list", null);
        
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<NotificationItem>>() {}.getType();
            notificationList.clear();
            notificationList.addAll(gson.fromJson(json, type));
            Collections.reverse(notificationList); // Newest first
        }

        if (notificationList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    private void markAllAsRead() {
        for (NotificationItem item : notificationList) {
            item.read = true;
        }
        saveNotifications();
        adapter.notifyDataSetChanged();
    }

    private void saveNotifications() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppNotifications", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        // Reverse back before saving to maintain original order in storage
        List<NotificationItem> toSave = new ArrayList<>(notificationList);
        Collections.reverse(toSave);
        editor.putString("notifications_list", gson.toJson(toSave));
        editor.apply();
    }

    public static class NotificationItem {
        public String title;
        public String message;
        public String time;
        public boolean read;

        public NotificationItem(String title, String message, String time) {
            this.title = title;
            this.message = message;
            this.time = time;
            this.read = false;
        }
    }
}
