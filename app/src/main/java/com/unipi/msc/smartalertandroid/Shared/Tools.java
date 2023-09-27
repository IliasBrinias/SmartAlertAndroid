package com.unipi.msc.smartalertandroid.Shared;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.unipi.msc.smartalertandroid.Activities.LoginActivity;
import com.unipi.msc.smartalertandroid.Model.User;
import com.unipi.msc.smartalertandroid.R;

public class Tools {
    public static User getUserFromMemory(Context context){
        Gson gson = new Gson();
        SharedPreferences sharedPref = context.getSharedPreferences(Tags.SHARED_PREF,Context.MODE_PRIVATE);
        return gson.fromJson(sharedPref.getString(Tags.USER, null), User.class);
    }
    public static String getTokenFromMemory(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(Tags.SHARED_PREF,Context.MODE_PRIVATE);
        return "Bearer " + sharedPref.getString(Tags.TOKEN, null);
    }
    public static void saveTokenToMemory(Context context, String token) {
        SharedPreferences sharedPref = context.getSharedPreferences(Tags.SHARED_PREF,Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        prefsEditor.putString(Tags.TOKEN, token);
        prefsEditor.apply();
    }
    public static void saveUserFromMemory(Context context, User user){
        SharedPreferences sharedPref = context.getSharedPreferences(Tags.SHARED_PREF,Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        prefsEditor.putString(Tags.USER, json);
        prefsEditor.apply();
    }
    public static void clearPreferences(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(Tags.SHARED_PREF,Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        prefsEditor.clear().apply();
    }
    public static void clearUser(Context context){
        clearPreferences(context);
        context.startActivity(new Intent(context,LoginActivity.class));
    }
    public static String handleErrorResponse(Context context, JsonObject jsonObject){
        if (!jsonObject.has("message")) {
            clearUser(context);
        }

        ErrorResponse errorResponse = new ErrorResponse(jsonObject.get("message").getAsString());
        switch (errorResponse.message){
            case ErrorMessages.ACCESS_DENIED:{
                return context.getString(R.string.access_denied);
            }
            case ErrorMessages.USERNAME_EXISTS:{
                return context.getString(R.string.username_exists);
            }
            case ErrorMessages.FILL_OBLIGATORY_FIELDS:{
                return context.getString(R.string.fill_obligatory_fields);
            }
            case ErrorMessages.USER_NOT_FOUND:{
                return context.getString(R.string.usern_not_found);
            }
            case ErrorMessages.RISK_NOT_FOUND: {
                return context.getString(R.string.risk_not_found);
            }
            case ErrorMessages.ALERT_NOT_FOUND: {
                return context.getString(R.string.alert_not_found);
            }
            case ErrorMessages.IMAGE_NOT_FOUND:{
                return context.getString(R.string.image_not_found);
            }
        }
        return null;
    }
    public static void showToast(Toast t, Context context , int msg_id){
        if (t!=null){
            t.cancel();
        }
        t = Toast.makeText(context,context.getString(msg_id), Toast.LENGTH_SHORT);
        t.show();
    }
}
