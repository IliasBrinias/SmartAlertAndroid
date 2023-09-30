package com.unipi.msc.smartalertandroid.Retrofit.Request;


import com.unipi.msc.smartalertandroid.Shared.DangerLevel;

import java.io.File;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class AlertRequest {
    private double latitude;
    private double longitude;
    private Long timestamp;
    private String comments;
    private Long disasterId;
    private DangerLevel dangerLevel;
    private File image;

    public AlertRequest(double latitude, double longitude, Long timestamp, String comments, Long disasterId, DangerLevel dangerLevel, File image) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.comments = comments;
        this.disasterId = disasterId;
        this.dangerLevel = dangerLevel;
        this.image = image;
    }

    public RequestBody getRequest(){
        MultipartBody.Builder builder =  new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("latitude", String.valueOf(latitude))
                .addFormDataPart("longitude", String.valueOf(longitude))
                .addFormDataPart("timestamp", String.valueOf(new Date().getTime()))
                .addFormDataPart("comments", this.comments)
                .addFormDataPart("disasterId", String.valueOf(this.disasterId))
                .addFormDataPart("dangerLevel", this.dangerLevel.name());
        if (this.image != null){
            builder.addFormDataPart("image", this.image.getName(), RequestBody.create(MediaType.parse("image/*"), this.image) );
        }
        return builder.build();
    }
}
