package com.unipi.msc.smartalertandroid.Retrofit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.unipi.msc.smartalertandroid.Retrofit.Request.AlertRequest;
import com.unipi.msc.smartalertandroid.Retrofit.Request.LoginRequest;
import com.unipi.msc.smartalertandroid.Retrofit.Request.RegisterRequest;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;

public interface APIInterface {
    @POST("/auth/login")
    Call<JsonObject> login(@Body LoginRequest request);
    @POST("/auth/register")
    Call<JsonObject> register(@Body RegisterRequest request);

    @GET("/alert")
    Call<JsonArray> getAlert(@Header("Authorization") String auth);

    @POST("/alert")
    Call<JsonObject> createAlert(@Body RequestBody request, @Header("Authorization") String auth);

    @GET("/risk")
    Call<JsonArray> getRisks(@Header("Authorization") String auth);

    @GET("/alert/notified")
    Call<JsonArray> getNotifiedAlerts(@Header("Authorization") String auth);

//    @POST("/api/users")
//    Call<User> createUser(@Body User user);
//
//    @GET("/api/users?")
//    Call<UserList> doGetUserList(@Query("page") String page);
//
//    @FormUrlEncoded
//    @POST("/api/users?")
//    Call<UserList> doCreateUserWithField(@Field("name") String name, @Field("job") String job);
}
