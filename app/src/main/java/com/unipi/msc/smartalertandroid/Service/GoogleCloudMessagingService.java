package com.unipi.msc.smartalertandroid.Service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.unipi.msc.smartalertandroid.Activities.AlertActivity;
import com.unipi.msc.smartalertandroid.R;
import com.unipi.msc.smartalertandroid.Shared.Tags;
import com.unipi.msc.smartalertandroid.Shared.Tools;

import java.util.HashMap;
import java.util.Map;

public class GoogleCloudMessagingService extends FirebaseMessagingService {
    private Handler mainHandler;
    private LocationManager locationManager;
    private LocationListener locationListener;
    long alertId;
    double latitude, longitude;
    String disaster, help;

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mainHandler = new Handler(Looper.getMainLooper());
        locationListener = location -> {
            double distance = Tools.distance(latitude, location.getLatitude(), longitude, location.getLongitude());
            if (distance <= Tags.DISTANCE_LIMIT) {
                showNotification(alertId, disaster, help);
            }
            locationManager.removeUpdates(locationListener);
        };
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();

        if (data.containsKey("alertId")) {
            alertId = Long.valueOf(data.get("alertId"));
        } else {
            alertId = 0;
        }
        if (data.containsKey("latitude")) {
            latitude = Double.valueOf(data.get("latitude"));
        } else {
            return;
        }
        if (data.containsKey("longitude")) {
            longitude = Double.valueOf(data.get("longitude"));
        } else {
            return;
        }

        disaster = data.getOrDefault("disaster", null);
        help = data.getOrDefault("help", null);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mainHandler.post(() -> locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener));
        }
    }

    @SuppressLint("MissingPermission")
    private void showNotification(Long alertId, String disaster, String help) {

        String channelId = "123";

        Intent intent = new Intent(this, AlertActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Tags.ALERT_ID, alertId);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_app_icon)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(disaster)
                .setContentInfo(Tools.getHelpInfo(this, help))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(Tools.getHelpInfo(this, help)))
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE))
                .setChannelId(channelId);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(123, builder.build());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        FirebaseMessaging.getInstance().subscribeToTopic(Tags.FIREBASE_CHANNEL);
    }
}
