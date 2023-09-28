package com.unipi.msc.smartalertandroid.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.unipi.msc.smartalertandroid.Model.Risk;
import com.unipi.msc.smartalertandroid.R;
import com.unipi.msc.smartalertandroid.Retrofit.APIInterface;
import com.unipi.msc.smartalertandroid.Retrofit.Request.AlertRequest;
import com.unipi.msc.smartalertandroid.Retrofit.RetrofitClient;
import com.unipi.msc.smartalertandroid.Shared.Tags;
import com.unipi.msc.smartalertandroid.Shared.Tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateAlertActivity extends AppCompatActivity implements LocationListener {
    ImageButton imageButtonExit;
    ImageView imageViewPhoto;
    EditText editTextComments;
    Button buttonSubmit;
    APIInterface apiInterface;
    Spinner spinnerRisk;
    LocationManager locationManager;
    Toast t;
    File file;
    Location currentLocation;
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (data == null) return;
                        if (data.hasExtra(Tags.FILE_PATH)) {
                            file = new File(data.getStringExtra(Tags.FILE_PATH));
                            Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                            try {
                                ExifInterface exif = new ExifInterface(file.getAbsolutePath());
                                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                                Matrix matrix = new Matrix();
                                if (orientation == 6) {
                                    matrix.postRotate(90);
                                } else if (orientation == 3) {
                                    matrix.postRotate(180);
                                } else if (orientation == 8) {
                                    matrix.postRotate(270);
                                }
                                myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true); // rotating bitmap
                            } catch (Exception ignore) {
                            }
                            imageViewPhoto.setImageBitmap(myBitmap);
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_alert);
        initViews();
        apiInterface = RetrofitClient.getInstance().create(APIInterface.class);
        getRisks();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    @SuppressLint("ResourceType")
    private void initViews() {
        imageButtonExit = findViewById(R.id.imageButtonExit);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        editTextComments = findViewById(R.id.editTextComments);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        spinnerRisk = findViewById(R.id.spinnerRisk);
        imageButtonExit.setOnClickListener(v->finish());
        buttonSubmit.setOnClickListener(this::createAlert);
        imageViewPhoto.setOnClickListener(this::OpenCamera);
    }
    List<Risk> riskList = new ArrayList<>();
    private void getRisks(){
        Call<JsonArray> call = apiInterface.getRisks(Tools.getTokenFromMemory(this));
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (!response.isSuccessful()){
                    if (response.code() == Tags.ACCESS_DENIED) {
                        Tools.clearPreferences(CreateAlertActivity.this);
                        startActivity(new Intent(CreateAlertActivity.this,LoginActivity.class));
                    }
                    return;
                }
                riskList.clear();
                List<String> risks = new ArrayList<>();
                for (JsonElement element : response.body()) {
                    Risk r = Risk.buildRisk(element.getAsJsonObject());
                    risks.add(r.getName());
                    riskList.add(r);
                }
                spinnerRisk.setAdapter(new ArrayAdapter<>(CreateAlertActivity.this, android.R.layout.simple_spinner_item, risks));
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
    }

    private void OpenCamera(View view) {
        someActivityResultLauncher.launch(new Intent(this, CameraActivity.class));
    }

    private void createAlert(View view) {
        if (spinnerRisk == null) return;
        if (currentLocation == null) return;
        AlertRequest request = new AlertRequest(currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                new Date().getTime(),
                editTextComments.getText().toString(),
                riskList.get(spinnerRisk.getSelectedItemPosition()).getId(),
                file);
        Call<JsonObject> call = apiInterface.createAlert(request.getRequest(),Tools.getTokenFromMemory(this));
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    Tools.showToast(t,CreateAlertActivity.this,R.string.alert_submited);
                    finish();
                } else {
                    Tools.handleErrorResponse(CreateAlertActivity.this, response.body());
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLocation = location;
    }
}