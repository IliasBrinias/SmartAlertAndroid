package com.unipi.msc.smartalertandroid.Activities;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.GoogleMap;
import com.google.gson.JsonObject;
import com.unipi.msc.smartalertandroid.R;
import com.unipi.msc.smartalertandroid.Retrofit.APIInterface;
import com.unipi.msc.smartalertandroid.Retrofit.RetrofitClient;
import com.unipi.msc.smartalertandroid.Shared.DangerLevel;
import com.unipi.msc.smartalertandroid.Shared.Role;
import com.unipi.msc.smartalertandroid.Shared.Tags;
import com.unipi.msc.smartalertandroid.Shared.Tools;
import com.unipi.msc.smartalertandroid.Model.Alert;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlertActivity extends AppCompatActivity {
    private ImageButton imageButtonExit;
    private ImageView imageViewPhoto, imageViewDanger;
    private TextView textViewName, textViewLocation,
                     textViewDisaster, textViewComments,
                     textViewHelp, textViewHelpTitle;
    private Button buttonNotify;
    private APIInterface apiInterface;
    private Toast t;
    private Alert alert;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        initViews();
        apiInterface = RetrofitClient.getInstance().create(APIInterface.class);
        Intent intent = getIntent();
        if (!intent.hasExtra(Tags.ALERT_ID)) finish();
        loadAlert(intent.getLongExtra(Tags.ALERT_ID,0));
    }

    private void loadAlert(Long alertId) {
        apiInterface.getAlert(alertId, Tools.getTokenFromMemory(this)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    try {
                        fillScreenData(Alert.buildAlert(response.body().get("data").getAsJsonObject()));
                    }catch (Exception ignore){}
                }else {
                    String msg = Tools.handleErrorResponse(AlertActivity.this,response);
                    Tools.showToast(t, AlertActivity.this, msg);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }
    private void fillScreenData(Alert alert){
        this.alert = alert;
        textViewLocation.setText(getFormattedAddress(alert.getLatitude(), alert.getLongitude()));
        textViewName.setText(alert.getUsername());
        textViewDisaster.setText(String.valueOf(alert.getDisaster().getName()));
        textViewComments.setText(alert.getComments());
        if (alert.getNotified()){
//            buttonNotify.setVisibility(View.GONE);
            textViewHelp.setVisibility(View.VISIBLE);
            textViewHelpTitle.setVisibility(View.VISIBLE);
            textViewHelp.setText(Tools.getHelpInfo(this, alert.getHelpInfo()));
        }
        if (alert.getDangerLevel() == DangerLevel.LOW){
            imageViewDanger.setImageResource(R.drawable.ic_alert_low);
        } else if (alert.getDangerLevel() == DangerLevel.MEDIUM) {
            imageViewDanger.setImageResource(R.drawable.ic_alert_medium);
        } else if (alert.getDangerLevel() == DangerLevel.HIGH) {
            imageViewDanger.setImageResource(R.drawable.ic_alert_high);
        }

        LazyHeaders lazyHeaders = new LazyHeaders.Builder()
                .addHeader("Authorization", Tools.getTokenFromMemory(this))
                .build();

        Glide.with(this)
            .load(new GlideUrl(Tags.API_URL + "/" + alert.getImage(),lazyHeaders))
            .centerCrop()
            .into(imageViewPhoto);
    }
    private String getFormattedAddress(double latitude, double longitude){
        try {
            Geocoder geocoder = new Geocoder(this);
            Address address = geocoder.getFromLocation(latitude, longitude, 1).get(0);
            return address.getAddressLine(0);
        } catch (IOException e) {
            return "";
        }
    }
    private void initViews() {
        imageButtonExit = findViewById(R.id.imageButtonExit);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        imageViewDanger = findViewById(R.id.imageViewDanger);
        textViewLocation = findViewById(R.id.textViewLocation);
        textViewName = findViewById(R.id.textViewName);
        textViewDisaster = findViewById(R.id.textViewDisaster);
        textViewComments = findViewById(R.id.textViewComments);
        buttonNotify = findViewById(R.id.buttonNotify);
        textViewHelp = findViewById(R.id.textViewHelp);
        textViewHelpTitle = findViewById(R.id.textViewHelpTitle);
        if (Tools.getUserFromMemory(this).getRole() == Role.OFFICER){
            buttonNotify.setVisibility(View.VISIBLE);
        }
        imageButtonExit.setOnClickListener(v->finish());
        buttonNotify.setOnClickListener(this::notifyAlert);
    }

    private void notifyAlert(View view) {
        apiInterface.notifyAlert(alert.getId(),Tools.getTokenFromMemory(this)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    Tools.showToast(t,AlertActivity.this, R.string.users_notified);
                    finish();
                }else {
                    String msg = Tools.handleErrorResponse(AlertActivity.this,response);
                    Tools.showToast(t, AlertActivity.this, msg);
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

}