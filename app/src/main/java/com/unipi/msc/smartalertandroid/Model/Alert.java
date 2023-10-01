package com.unipi.msc.smartalertandroid.Model;


import com.google.gson.JsonObject;
import com.unipi.msc.smartalertandroid.Shared.DangerLevel;

public class Alert {
    private Long Id;
    private double latitude;
    private double longitude;
    private Long timestamp;
    private DangerLevel dangerLevel;
    private String comments;
    private Boolean notified;
    private String username;
    private Disaster disaster;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Disaster getDisaster() {
        return disaster;
    }

    public void setDisaster(Disaster disaster) {
        this.disaster = disaster;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public DangerLevel getDangerLevel() {
        return dangerLevel;
    }

    public void setDangerLevel(DangerLevel dangerLevel) {
        this.dangerLevel = dangerLevel;
    }

    public static Alert buildAlert(JsonObject jsonObject) {
        Alert alert = new Alert();

        try {
            alert.setLatitude(jsonObject.get("latitude").getAsDouble());
        }catch (Exception ignore){}

        try {
            alert.setLongitude(jsonObject.get("longitude").getAsDouble());
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
            alert.setUsername(jsonObject.get("username").getAsString());
        }catch (Exception ignore){}

        try {
            alert.setDisaster(Disaster.buildDisaster(jsonObject.get("disaster").getAsJsonObject()));
        }catch (Exception ignore){}

        try {
            alert.setId(jsonObject.get("id").getAsLong());
        }catch (Exception ignore){}

        try {
            alert.setImage(jsonObject.get("image").getAsString());
        }catch (Exception ignore){}

        try {
            alert.setDangerLevel(DangerLevel.valueOf(jsonObject.get("dangerLevel").getAsString()));
        }catch (Exception ignore){}

        return alert;
    }
}
