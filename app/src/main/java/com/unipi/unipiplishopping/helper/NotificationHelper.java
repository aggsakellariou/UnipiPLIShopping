package com.unipi.unipiplishopping.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.navigation.NavDeepLinkBuilder;

import com.unipi.unipiplishopping.R;

import java.util.Calendar;
import java.util.UUID;

public class NotificationHelper {
    private static final String CHANNEL_ID = "my_channel";
    private static final int NOTIFICATION_ID = 1;
    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    // Show a notification when a product is near the user
    public void showNotification(String productTitle, String productId) {
        if (!shouldShowNotification(productId)) {
            return;
        }

        String message = context.getString(R.string.product_near_you, productTitle);
        long timestamp = System.currentTimeMillis();

        // Save the message, timestamp, and productId to SharedPreferences with a unique key
        SharedPreferences sharedPreferences = context.getSharedPreferences("notifications", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String uniqueKey = UUID.randomUUID().toString();
        editor.putString(uniqueKey + "_message", message);
        editor.putLong(uniqueKey + "_timestamp", timestamp);
        editor.putString(uniqueKey + "_productId", productId);
        editor.apply();

        // Create the notification
        Bundle args = new Bundle();
        args.putString("productTitle", productTitle);
        args.putString("productId", productId);

        PendingIntent pendingIntent = new NavDeepLinkBuilder(context)
                .setGraph(R.navigation.mobile_navigation)
                .setDestination(R.id.productDetailsFragment)
                .setArguments(args)
                .createPendingIntent();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle(context.getString(R.string.nearby_store))
                .setContentText(context.getString(R.string.product_near_you, productTitle))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_ONLY_ALERT_ONCE;
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private boolean shouldShowNotification(String productId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("notifications", Context.MODE_PRIVATE);
        long lastNotificationTime = sharedPreferences.getLong(productId, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(lastNotificationTime);

        Calendar now = Calendar.getInstance();
        boolean sameDay = calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR);

        if (sameDay) {
            return false;
        }

        // Save the current timestamp
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(productId, now.getTimeInMillis());
        editor.apply();

        return true;
    }

    // Create a notification channel
    private void createNotificationChannel() {
        CharSequence channelName = "My Channel";
        String channelDescription = "My Channel Description";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
        channel.setDescription(channelDescription);

        notificationManager.createNotificationChannel(channel);
    }
}