package com.unipi.msc.smartalertandroid.Retrofit.Request;


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
    private Long riskId;
    private File image;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Long getRiskId() {
        return riskId;
    }

    public void setRiskId(Long riskId) {
        this.riskId = riskId;
    }

    public File getImage() {
        return image;
    }

    public void setImage(File image) {
        this.image = image;
    }

    public AlertRequest(double latitude, double longitude, Long timestamp, String comments, Long riskId, File image) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.comments = comments;
        this.riskId = riskId;
        this.image = image;
    }

    public RequestBody getRequest(){
        return new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("latitude", String.valueOf(0))
            .addFormDataPart("longitude", String.valueOf(0))
            .addFormDataPart("timestamp", String.valueOf(new Date().getTime()))
            .addFormDataPart("comments", this.comments)
            .addFormDataPart("image", this.image.getName(), RequestBody.create(MediaType.parse("image/*"), this.image) )
            .build();
    }
}
