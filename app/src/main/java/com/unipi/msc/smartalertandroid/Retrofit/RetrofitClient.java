package com.unipi.msc.smartalertandroid.Retrofit;

import android.content.Context;

import com.unipi.msc.smartalertandroid.Shared.Tools;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    public static Retrofit getInstance(){
        return new Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8081")
            .addConverterFactory(GsonConverterFactory.create())
            .client(new OkHttpClient().newBuilder().build())
            .build();
    }
}
