package com.unipi.unipiplishopping.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.unipiplishopping.R;
import com.unipi.unipiplishopping.models.NotificationItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private final List<NotificationItem> notificationList;

    public NotificationAdapter(List<NotificationItem> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem notification = notificationList.get(position);
        holder.notificationTextView.setText(notification.getMessage());
        holder.timestampTextView.setText(formatTimestamp(notification.getTimestamp()));

        // Set click listener for navigation
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("productId", notification.getProductId());

            // Navigate to ProductDetailsFragment
            Navigation.findNavController(v).navigate(R.id.action_navigation_notifications_to_productDetailsFragment, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    // Format timestamp to readable format
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView notificationTextView;
        public TextView timestampTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            notificationTextView = itemView.findViewById(R.id.notification_text);
            timestampTextView = itemView.findViewById(R.id.timestamp_text);
        }
    }
}