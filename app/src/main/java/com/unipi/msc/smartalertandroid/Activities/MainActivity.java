package com.unipi.msc.smartalertandroid.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.unipi.msc.smartalertandroid.Model.User;
import com.unipi.msc.smartalertandroid.R;
import com.unipi.msc.smartalertandroid.Retrofit.APIInterface;
import com.unipi.msc.smartalertandroid.Retrofit.RetrofitClient;
import com.unipi.msc.smartalertandroid.Shared.Tools;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_LOCATION_CODE = 123;
    private TextView textViewTitle;
    private User user;
    private APIInterface apiInterface;
    private LinearLayout linearLayoutAlert, linearLayoutMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        apiInterface = RetrofitClient.getInstance(this).create(APIInterface.class);
        user = Tools.getUserFromMemory(this);
        initViews();
        gpsPermission();


    }

    private void gpsPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQ_LOCATION_CODE);
        }
    }

    private void initViews() {
        textViewTitle = findViewById(R.id.textViewTitle);
        linearLayoutAlert = findViewById(R.id.linearLayoutAlert);
        linearLayoutMap = findViewById(R.id.linearLayoutMap);
        linearLayoutAlert.setOnClickListener(this::showAlertActivity);
        linearLayoutAlert.setOnClickListener(this::showMapActivity);
        textViewTitle.setText(getString(R.string.hello)+" "+user.getName());
    }

    private void showMapActivity(View view) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION_CODE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
        }
    }

    private void showAlertActivity(View view) {
        startActivity(new Intent(this, CreateAlertActivity.class));
    }
}