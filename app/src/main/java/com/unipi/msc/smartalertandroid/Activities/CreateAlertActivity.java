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
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.unipi.msc.smartalertandroid.Model.Disaster;
import com.unipi.msc.smartalertandroid.R;
import com.unipi.msc.smartalertandroid.Retrofit.APIInterface;
import com.unipi.msc.smartalertandroid.Retrofit.Request.AlertRequest;
import com.unipi.msc.smartalertandroid.Retrofit.RetrofitClient;
import com.unipi.msc.smartalertandroid.Shared.DangerLevel;
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
    Spinner spinnerDisaster;
    LocationManager locationManager;
    Toast t;
    File file;
    Location currentLocation;
    RadioGroup radioGroupDanger;
    List<Disaster> disasterList = new ArrayList<>();

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
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
        getDisasters();
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
        spinnerDisaster = findViewById(R.id.spinnerDisaster);
        radioGroupDanger = findViewById(R.id.radioGroupDanger);
        imageButtonExit.setOnClickListener(v->finish());
        buttonSubmit.setOnClickListener(this::createAlert);
        imageViewPhoto.setOnClickListener(this::OpenCamera);
    }
    private void getDisasters(){
        Call<JsonObject> call = apiInterface.getDisasters(Tools.getTokenFromMemory(this));
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    try {
                        disasterList.clear();
                        List<String> disasters = new ArrayList<>();
                        for (JsonElement element : response.body().get("data").getAsJsonArray()) {
                            Disaster r = Disaster.buildDisaster(element.getAsJsonObject());
                            disasters.add(r.getName());
                            disasterList.add(r);
                        }
                        spinnerDisaster.setAdapter(new ArrayAdapter<>(CreateAlertActivity.this, android.R.layout.simple_spinner_item, disasters));
                    }catch (Exception ignore){}
                }else{
                    String msg = Tools.handleErrorResponse(CreateAlertActivity.this,response);
                    Tools.showToast(t, CreateAlertActivity.this, msg);
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
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
        if (spinnerDisaster == null) return;
        if (currentLocation == null) return;
        AlertRequest request = new AlertRequest(currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                new Date().getTime(),
                editTextComments.getText().toString(),
                disasterList.get(spinnerDisaster.getSelectedItemPosition()).getId(),
                getDangerLevel(),
                file);
        Call<JsonObject> call = apiInterface.createAlert(request.getRequest(),Tools.getTokenFromMemory(this));
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    Tools.showToast(t,CreateAlertActivity.this,R.string.alert_submited);
                    finish();
                } else {
                    String msg = Tools.handleErrorResponse(CreateAlertActivity.this,response);
                    Tools.showToast(t, CreateAlertActivity.this, msg);
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private DangerLevel getDangerLevel() {
        DangerLevel dangerLevel = DangerLevel.MEDIUM;
        int checkedRadioButtonId = radioGroupDanger.getCheckedRadioButtonId();
        if (checkedRadioButtonId == R.id.radioButtonLow) {
            dangerLevel = DangerLevel.LOW;
        } else if (checkedRadioButtonId == R.id.radioButtonMedium) {
            dangerLevel = DangerLevel.MEDIUM;
        } else if (checkedRadioButtonId == R.id.radioButtonHigh) {
            dangerLevel = DangerLevel.HIGH;
        }
        return dangerLevel;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLocation = location;
    }
}