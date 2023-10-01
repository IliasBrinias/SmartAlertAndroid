package com.unipi.msc.smartalertandroid.Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.unipi.msc.smartalertandroid.Shared.Tags;

import java.util.HashMap;
import java.util.Map;

public class GoogleCloudMessagingService extends FirebaseMessagingService {
    NotificationManager mNotificationManager;
    static Map<String,Integer> requestAccepted = new HashMap<>();
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        System.out.println(remoteMessage.getData());
        if (true) return;
        String messageSessionId = remoteMessage.getData().get("body");
        String channelId = remoteMessage.getData().get("title");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID");
//        Intent resultIntent = new Intent(this, MessengerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(), PendingIntent.FLAG_IMMUTABLE);
        builder.setContentTitle(remoteMessage.getNotification().getTitle());
        builder.setContentText(remoteMessage.getNotification().getBody());
//        builder.setSmallIcon(R.drawable.ic_car_notification_icon);
        builder.setContentIntent(pendingIntent);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody()));
        builder.setAutoCancel(true);
        mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH);
        mNotificationManager.createNotificationChannel(channel);
        builder.setChannelId(channelId);
        mNotificationManager.notify(requestAccepted.get(messageSessionId),builder.build());
    }
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        FirebaseMessaging.getInstance().subscribeToTopic(Tags.FIREBASE_CHANNEL);
    }
}
