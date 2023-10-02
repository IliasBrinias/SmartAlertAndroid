package com.unipi.msc.smartalertandroid.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.unipi.msc.smartalertandroid.Model.Alert;
import com.unipi.msc.smartalertandroid.Model.User;
import com.unipi.msc.smartalertandroid.R;
import com.unipi.msc.smartalertandroid.Retrofit.APIInterface;
import com.unipi.msc.smartalertandroid.Retrofit.RetrofitClient;
import com.unipi.msc.smartalertandroid.Shared.Role;
import com.unipi.msc.smartalertandroid.Shared.Tags;
import com.unipi.msc.smartalertandroid.Shared.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private static final int REQ_LOCATION_CODE = 123;
    private TextView textViewTitle;
    private User user;
    private ImageButton imageButtonLogout;
    private Button buttonCreateAlert;
    private MapView mapView;
    private GoogleMap googleMap;
    private APIInterface apiInterface;
    private List<Circle> circles = new ArrayList<>();
    private Toast t;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        apiInterface = RetrofitClient.getInstance().create(APIInterface.class);
        user = Tools.getUserFromMemory(this);
        initViews();
        // Initialize the MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        gpsPermission();
        FirebaseMessaging.getInstance().subscribeToTopic(Tags.FIREBASE_CHANNEL);
    }

    private void storagePermission() {
        if (Build.VERSION.SDK_INT >= 30){
            if (!Environment.isExternalStorageManager()){
                Intent getpermission = new Intent();
                getpermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(getpermission);
            }
        }
    }

    private void gpsPermission() {
        List<String> permissions = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        if (Build.VERSION.SDK_INT >= 30){
            if (!Environment.isExternalStorageManager()){
                Intent getpermission = new Intent();
                getpermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(getpermission);
            }
        }
        if (permissions.isEmpty()) return;
        ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), REQ_LOCATION_CODE);
    }

    private void initViews() {
        textViewTitle = findViewById(R.id.textViewTitle);
        buttonCreateAlert = findViewById(R.id.buttonCreateAlert);
        imageButtonLogout = findViewById(R.id.imageButtonLogout);
        imageButtonLogout.setOnClickListener(this::logout);
        buttonCreateAlert.setOnClickListener(this::showAlertActivity);
        textViewTitle.setText(getString(R.string.hello)+" "+user.getName());
    }

    private void logout(View view) {
        Tools.clearPreferences(this);
        startActivity(new Intent(this,LoginActivity.class));
    }

    private void showAlertActivity(View view) {
        startActivity(new Intent(this, CreateAlertActivity.class));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.9838, 23.7275), 6));
        apiInterface.getNotifiedAlerts(Tools.getTokenFromMemory(this)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    try {
                        List<Alert> alerts = new ArrayList<>();
                        if (response.body().size() == 0) return;
                        for (JsonElement element : response.body().get("data").getAsJsonArray()) {
                            alerts.add(Alert.buildAlert(element.getAsJsonObject()));
                        }
                        showAlertedAreas(alerts);
                    }catch (Exception ignore){}
                }else {
                    String msg = Tools.handleErrorResponse(MainActivity.this,response);
                    Tools.showToast(t, MainActivity.this, msg);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                t.printStackTrace();
            }
        });
        if (user.getRole() != Role.OFFICER) return;
        apiInterface.getAlert(Tools.getTokenFromMemory(this)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    try {
                        List<Alert> alerts = new ArrayList<>();
                        for (JsonElement element : response.body().get("data").getAsJsonArray()) {
                            alerts.add(Alert.buildAlert(element.getAsJsonObject()));
                        }
                        showPendingAlerts(alerts);
                    }catch (Exception ignore){}
                } else {
                    String msg = Tools.handleErrorResponse(MainActivity.this,response);
                    Tools.showToast(t, MainActivity.this, msg);
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void showPendingAlerts(List<Alert> alerts) {
        for (Alert alert:alerts) {
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(alert.getLatitude(), alert.getLongitude()))
                    .title(alert.getDisaster().getName())
            );
            marker.setTag(alert.getId());
        }
    }

    private void showAlertedAreas(List<Alert> alerts) {
        for (Alert alert:alerts) {
            circles.add(googleMap.addCircle(new CircleOptions()
                .center(new LatLng(alert.getLatitude(), alert.getLongitude()))
                .radius(Tags.CIRCLE_RADIUS)
                .fillColor(Color.BLUE)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        if (marker.getTag() instanceof Long){
            Long alertId = (Long) marker.getTag();
            Intent intent = new Intent(this, AlertActivity.class);
            intent.putExtra(Tags.ALERT_ID, alertId);
            startActivity(intent);
        }
    }
    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}