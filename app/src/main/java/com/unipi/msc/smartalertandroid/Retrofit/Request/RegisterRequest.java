package com.unipi.msc.smartalertandroid.Retrofit.Request;


import com.unipi.msc.smartalertandroid.Shared.Role;

public class RegisterRequest {
    private String username;
    private String password;
    private String name;
    private String role;

    public String getUsername() {
        return username;
    }
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RegisterRequest(String username, String password, String name) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = Role.CITIZEN.toString();
    }

}
