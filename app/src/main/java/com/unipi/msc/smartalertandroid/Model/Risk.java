package com.unipi.msc.smartalertandroid.Model;


import com.google.gson.JsonObject;
import com.unipi.msc.smartalertandroid.Shared.DangerLevel;

public class Risk {
    private Long id;
    private String name;
    private DangerLevel dangerLevel;

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

    public DangerLevel getDangerLevel() {
        return dangerLevel;
    }

    public void setDangerLevel(DangerLevel dangerLevel) {
        this.dangerLevel = dangerLevel;
    }

    public static Risk buildRisk(JsonObject jsonObject){
        Risk risk = new Risk();

        try {
            risk.setId(jsonObject.get("id").getAsLong());
        }catch (Exception ignore){}

        try {
            risk.setName(jsonObject.get("name").getAsString());
        }catch (Exception ignore){}

        try {
            risk.setDangerLevel(DangerLevel.valueOf(jsonObject.get("dangerLevel").getAsString()));
        }catch (Exception ignore){}
        return risk;
    }
}
