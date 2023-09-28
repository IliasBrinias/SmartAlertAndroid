package com.unipi.msc.smartalertandroid.Model;


import com.google.gson.JsonObject;

public class Alert {
    private Long Id;
    private double latitude;
    private double longitude;
    private Long timestamp;
    private String comments;
    private Boolean notified;
    private Long userId;
    private Risk risk;
    private String image;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

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

    public Boolean getNotified() {
        return notified;
    }

    public void setNotified(Boolean notified) {
        this.notified = notified;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Risk getRisk() {
        return risk;
    }

    public void setRisk(Risk risk) {
        this.risk = risk;
    }

    public static Alert buildAlert(JsonObject jsonObject) {
        Alert alert = new Alert();

        try {
            alert.setLatitude(jsonObject.get("latitude").getAsLong());
        }catch (Exception ignore){}

        try {
            alert.setLongitude(jsonObject.get("longitude").getAsLong());
        }catch (Exception ignore){}

        try {
            alert.setTimestamp(jsonObject.get("timestamp").getAsLong());
        }catch (Exception ignore){}

        try {
            alert.setNotified(jsonObject.get("notified").getAsBoolean());
        }catch (Exception ignore){}

        try {
            alert.setComments(jsonObject.get("comments").getAsString());
        }catch (Exception ignore){}

        try {
            alert.setUserId(jsonObject.get("userId").getAsLong());
        }catch (Exception ignore){}

        try {
            alert.setRisk(Risk.buildRisk(jsonObject.get("risk").getAsJsonObject()));
        }catch (Exception ignore){}

        try {
            alert.setId(jsonObject.get("id").getAsLong());
        }catch (Exception ignore){}

        try {
            alert.setImage(jsonObject.get("image").getAsString());
        }catch (Exception ignore){}

        return alert;
    }
}
