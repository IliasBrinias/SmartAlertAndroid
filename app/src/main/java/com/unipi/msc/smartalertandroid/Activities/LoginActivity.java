package com.unipi.msc.smartalertandroid.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.unipi.msc.smartalertandroid.Model.User;
import com.unipi.msc.smartalertandroid.R;
import com.unipi.msc.smartalertandroid.Retrofit.APIInterface;
import com.unipi.msc.smartalertandroid.Retrofit.Request.LoginRequest;
import com.unipi.msc.smartalertandroid.Retrofit.Request.RegisterRequest;
import com.unipi.msc.smartalertandroid.Retrofit.RetrofitClient;
import com.unipi.msc.smartalertandroid.Shared.ErrorResponse;
import com.unipi.msc.smartalertandroid.Shared.Tags;
import com.unipi.msc.smartalertandroid.Shared.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    EditText editTextUsername,
             editTextPassword,
             editTextPasswordVerify,
             editTextName;
    Button buttonLogin, buttonRegister;
    APIInterface apiInterface;
    Callback<JsonObject> handleResponse;
    Toast t;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        if (Tools.getUserFromMemory(this) != null){
            openMainActivity();
        }
        apiInterface = RetrofitClient.getInstance().create(APIInterface.class);
    }

    private void initViews() {
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPasswordVerify = findViewById(R.id.editTextPasswordVerify);
        editTextName = findViewById(R.id.editTextName);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonRegister.setOnClickListener(this::register);
        buttonLogin.setOnClickListener(this::login);
        handleResponse = new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    User user = User.buildUser(response.body());
                    Tools.saveUserFromMemory(LoginActivity.this, user);
                    try {
                        Tools.saveTokenToMemory(LoginActivity.this, response.body().get("token").getAsString());
                    }catch (Exception ignore){}
                    openMainActivity();
                }else{
                    Tools.showToast(t,LoginActivity.this,R.string.something_happened);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {
                throwable.printStackTrace();
                Tools.showToast(t,LoginActivity.this,R.string.something_happened);
            }
        };
    }

    private void register(View view) {
        RegisterRequest request = new RegisterRequest(editTextUsername.getText().toString(),
                                                      editTextPassword.getText().toString(),
                                                      editTextName.getText().toString());
        Call<JsonObject> call = apiInterface.register(request);
        call.enqueue(handleResponse);
    }

    private void login(View view) {
        LoginRequest request = new LoginRequest(editTextUsername.getText().toString(),
                                                editTextPassword.getText().toString());
        Call<JsonObject> call = apiInterface.login(request);
        call.enqueue(handleResponse);
    }
    private void openMainActivity(){
        startActivity(new Intent(LoginActivity.this,MainActivity.class));
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}