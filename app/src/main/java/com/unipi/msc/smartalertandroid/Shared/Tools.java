package com.unipi.msc.smartalertandroid.Shared;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.unipi.msc.smartalertandroid.Activities.LoginActivity;
import com.unipi.msc.smartalertandroid.Activities.MainActivity;
import com.unipi.msc.smartalertandroid.Model.User;
import com.unipi.msc.smartalertandroid.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Response;

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
    public static void clearUser(Activity activity){
        clearPreferences(activity);
        activity.startActivity(new Intent(activity,LoginActivity.class));
        activity.finish();
    }
    public static String handleErrorResponse(Activity activity, Response<JsonObject> response){
        if (response.code() == 403) clearUser(activity);
        try {
            JSONObject jObjError = new JSONObject(response.errorBody().string());
            switch (jObjError.getString("message")){
                case ErrorMessages.ACCESS_DENIED:{
                    return activity.getString(R.string.access_denied);
                }
                case ErrorMessages.USERNAME_EXISTS:{
                    return activity.getString(R.string.username_exists);
                }
                case ErrorMessages.FILL_OBLIGATORY_FIELDS:{
                    return activity.getString(R.string.fill_obligatory_fields);
                }
                case ErrorMessages.USER_NOT_FOUND:{
                    return activity.getString(R.string.usern_not_found);
                }
                case ErrorMessages.RISK_NOT_FOUND: {
                    return activity.getString(R.string.risk_not_found);
                }
                case ErrorMessages.ALERT_NOT_FOUND: {
                    return activity.getString(R.string.alert_not_found);
                }
                case ErrorMessages.IMAGE_NOT_FOUND:{
                    return activity.getString(R.string.image_not_found);
                }
            }
        } catch (JSONException|IOException ignore) {}
        return null;
    }
    public static void showToast(Toast t, Context context , int msg_id){
        showToast(t, context, context.getString(msg_id));
    }
    public static void showToast(Toast t, Context context, String msg){
        if (t!=null){
            t.cancel();
        }
        if (msg == null) return;
        t = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        t.show();
    }
    public static double distance(double lat1, double lat2,
                                  double lon1, double lon2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters


        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }

    public static String getHelpInfo(Context context, String help) {
        switch (help){
            case "Flood": return context.getString(R.string.flood_help);
            case "Rain": return context.getString(R.string.rain_help);
            case "Τornado": return context.getString(R.string.tornado_help);
            case "Εarthquake": return context.getString(R.string.earthquake_help);
            default: return "";
        }
    }
}
