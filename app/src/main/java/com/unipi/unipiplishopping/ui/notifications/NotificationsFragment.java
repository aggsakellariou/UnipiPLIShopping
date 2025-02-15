package com.unipi.unipiplishopping.ui.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.unipiplishopping.databinding.FragmentNotificationsBinding;
import com.unipi.unipiplishopping.models.NotificationItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class NotificationsFragment extends Fragment {
    private FragmentNotificationsBinding binding;
    private NotificationAdapter notificationAdapter;
    private List<NotificationItem> notificationList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(notificationAdapter);
        loadNotifications();

        // Add ItemTouchHelper for swipe-to-delete functionality
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                deleteNotification(position);
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return root;
    }

    // Load notifications from SharedPreferences
    private void loadNotifications() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("notifications", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            if (key.endsWith("_message")) {
                String message = entry.getValue().toString();
                long timestamp = sharedPreferences.getLong(key.replace("_message", "_timestamp"), 0);
                String productId = sharedPreferences.getString(key.replace("_message", "_productId"), "");
                notificationList.add(new NotificationItem(message, timestamp, productId));
            }
        }
        // Sort notifications by timestamp
        notificationList.sort(new Comparator<NotificationItem>() {
            @Override
            public int compare(NotificationItem o1, NotificationItem o2) {
                return Long.compare(o2.getTimestamp(), o1.getTimestamp());
            }
        });
        notificationAdapter.notifyDataSetChanged();
    }

    // Delete notification from RecyclerView and SharedPreferences
    private void deleteNotification(int position) {
        NotificationItem notificationItem = notificationList.get(position);
        notificationList.remove(position);
        notificationAdapter.notifyItemRemoved(position);

        // Remove from SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("notifications", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Find the correct keys to remove
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            if (key.endsWith("_message") && entry.getValue().equals(notificationItem.getMessage())) {
                String uniqueKey = key.replace("_message", "");
                editor.remove(uniqueKey + "_message");
                editor.remove(uniqueKey + "_timestamp");
                editor.remove(uniqueKey + "_productId");
                break;
            }
        }
        editor.apply();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}