package com.unipi.msc.smartalertandroid.Model;


import com.google.gson.JsonObject;
import com.unipi.msc.smartalertandroid.Shared.Role;

public class User {
    private Long id;
    private String username;
    private String name;
    private Role role;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public static User buildUser(JsonObject jsonObject){
        User user = new User();

        try {
            user.setId(jsonObject.get("id").getAsLong());
        }catch (Exception ignore){}

        try {
            user.setUsername(jsonObject.get("username").getAsString());
        }catch (Exception ignore){}

        try {
            user.setName(jsonObject.get("name").getAsString());
        }catch (Exception ignore){}

        try {
            user.setRole(Role.valueOf(jsonObject.get("role").getAsString()));
        }catch (Exception ignore){}

        return user;
    }
}
