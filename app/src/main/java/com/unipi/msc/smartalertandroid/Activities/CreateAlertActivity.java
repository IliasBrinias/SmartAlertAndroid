package com.unipi.msc.smartalertandroid.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.gson.JsonObject;
import com.unipi.msc.smartalertandroid.R;
import com.unipi.msc.smartalertandroid.Retrofit.APIInterface;
import com.unipi.msc.smartalertandroid.Retrofit.Request.AlertRequest;
import com.unipi.msc.smartalertandroid.Retrofit.RetrofitClient;
import com.unipi.msc.smartalertandroid.Shared.Tools;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateAlertActivity extends AppCompatActivity {
    ImageButton imageButtonExit;
    ImageView imageViewPhoto;
    EditText editTextComments;
    Button buttonSubmit;
    APIInterface apiInterface;
    Spinner spinnerRisk;
    LocationManager locationManager;
    Toast t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_alert);
        initViews();
        apiInterface = RetrofitClient.getInstance(this).create(APIInterface.class);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @SuppressLint("ResourceType")
    private void initViews() {
        imageButtonExit = findViewById(R.id.imageButtonExit);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        editTextComments = findViewById(R.id.editTextComments);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        spinnerRisk = findViewById(R.id.spinnerRisk);
        buttonSubmit.setOnClickListener(this::createAlert);
        spinnerRisk.setAdapter(new ArrayAdapter<String>(this, R.array.risk_array));
        spinnerRisk.setOnItemClickListener((parent, view, position, id) -> {

        });
    }

    private void createAlert(View view) {
        Location currentLocation = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (currentLocation == null) return;
        AlertRequest request = new AlertRequest(currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                new Date().getTime(),
                editTextComments.getText().toString(),
                0L,
                null);
        Call<JsonObject> call = apiInterface.createAlert(request.getRequest(),Tools.getTokenFromMemory(this));
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    Tools.showToast(t,CreateAlertActivity.this,R.string.alert_submited);
                } else {
                    Tools.showToast(t,CreateAlertActivity.this,R.string.alert_submited);
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {}
        });
    }
}