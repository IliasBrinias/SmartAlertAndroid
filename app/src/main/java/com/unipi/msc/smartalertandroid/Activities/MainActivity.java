package com.unipi.msc.smartalertandroid.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.unipi.msc.smartalertandroid.Model.Alert;
import com.unipi.msc.smartalertandroid.Model.User;
import com.unipi.msc.smartalertandroid.R;
import com.unipi.msc.smartalertandroid.Retrofit.APIInterface;
import com.unipi.msc.smartalertandroid.Retrofit.RetrofitClient;
import com.unipi.msc.smartalertandroid.Shared.DangerLevel;
import com.unipi.msc.smartalertandroid.Shared.Role;
import com.unipi.msc.smartalertandroid.Shared.Tags;
import com.unipi.msc.smartalertandroid.Shared.Tools;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int REQ_LOCATION_CODE = 123;
    private TextView textViewTitle;
    private User user;
    private ImageButton imageButtonLogout, imageButtonRefresh, imageButtonStatistics;
    private Button buttonCreateAlert;
    private MapView mapView;
    private GoogleMap googleMap;
    private APIInterface apiInterface;
    private List<Circle> circles = new ArrayList<>();
    private List<Marker> markers = new ArrayList<>();
    Toast t;
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
        permissions();
        FirebaseMessaging.getInstance().subscribeToTopic(Tags.FIREBASE_CHANNEL);
    }

    private void permissions() {
        List<String> permissions = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        if (Build.VERSION.SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
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
        imageButtonRefresh = findViewById(R.id.imageButtonRefresh);
        imageButtonStatistics = findViewById(R.id.imageButtonStatistics);
        imageButtonLogout.setOnClickListener(this::logout);
        buttonCreateAlert.setOnClickListener(this::showAlertActivity);
        imageButtonStatistics.setOnClickListener(this::showStatisticsActivity);
        imageButtonRefresh.setOnClickListener(v->refreshData());
        textViewTitle.setText(getString(R.string.hello) + " " + user.getName());
    }

    private void showStatisticsActivity(View view) {
        startActivity(new Intent(this, StatisticsActivity.class));
    }

    private void refreshData() {
        if (googleMap == null) return;
        clearMapElements();
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
        if (user.getRole() == Role.OFFICER) {
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
                        } catch (Exception ignore) {
                        }
                    } else {
                        String msg = Tools.handleErrorResponse(MainActivity.this, response);
                        Tools.showToast(t, MainActivity.this, msg);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }

    private void clearMapElements() {
        for (Circle c:circles) c.remove();
        for (Marker m:markers) m.remove();
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.setPadding(0,0,0,Tools.dpToPx(MainActivity.this, 55));

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.9838, 23.7275), 6));
        googleMap.setOnCircleClickListener(circle -> openAlertActivity((Long) circle.getTag()));
        googleMap.setOnMarkerClickListener(marker -> {
            openAlertActivity((Long) marker.getTag());
            return true;
        });
        refreshData();
    }

    private void showPendingAlerts(List<Alert> alerts) {
        for (Alert alert:alerts) {
            BitmapDescriptor iconBitmap;
            if (alert.getDangerLevel() == DangerLevel.LOW){
                iconBitmap = Tools.getBitmapDescriptor(this,R.drawable.ic_alert_low);
            } else if (alert.getDangerLevel() == DangerLevel.MEDIUM) {
                iconBitmap = Tools.getBitmapDescriptor(this,R.drawable.ic_alert_medium);
            }else{
                iconBitmap = Tools.getBitmapDescriptor(this,R.drawable.ic_alert_high);
            }
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(alert.getLatitude(), alert.getLongitude()))
                    .title(alert.getDisaster().getName())
                    .icon(iconBitmap)
            );
            marker.setTag(alert.getId());
            markers.add(marker);
        }
    }

    private void showAlertedAreas(List<Alert> alerts) {
        for (Alert alert:alerts) {
            Circle circle = googleMap.addCircle(new CircleOptions()
                .center(new LatLng(alert.getLatitude(), alert.getLongitude()))
                .radius(Tags.CIRCLE_RADIUS)
                .clickable(true)
                .fillColor(getColor(R.color.color_alerted)));
            circle.setTag(alert.getId());
            circles.add(circle);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        refreshData();
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

    private void openAlertActivity(Long alertId) {
        Intent intent = new Intent(this, AlertActivity.class);
        intent.putExtra(Tags.ALERT_ID, alertId);
        startActivity(intent);
    }

    private void logout(View view) {
        Tools.clearPreferences(this);
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void showAlertActivity(View view) {
        startActivity(new Intent(this, CreateAlertActivity.class));
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}