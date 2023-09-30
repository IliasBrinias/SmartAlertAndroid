package com.unipi.msc.smartalertandroid.Model;


import com.google.gson.JsonObject;
import com.unipi.msc.smartalertandroid.Shared.DangerLevel;

public class Disaster {
    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Disaster buildDisaster(JsonObject jsonObject){
        Disaster disaster = new Disaster();

        try {
            disaster.setId(jsonObject.get("id").getAsLong());
        }catch (Exception ignore){}

        try {
            disaster.setName(jsonObject.get("name").getAsString());
        }catch (Exception ignore){}

        return disaster;
    }
}
