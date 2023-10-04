package com.unipi.msc.smartalertandroid.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.unipi.msc.smartalertandroid.Model.User;
import com.unipi.msc.smartalertandroid.R;
import com.unipi.msc.smartalertandroid.Retrofit.APIInterface;
import com.unipi.msc.smartalertandroid.Retrofit.Request.LoginRequest;
import com.unipi.msc.smartalertandroid.Retrofit.Request.RegisterRequest;
import com.unipi.msc.smartalertandroid.Retrofit.RetrofitClient;
import com.unipi.msc.smartalertandroid.Shared.Tools;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    EditText editTextUsername, editTextPassword, editTextName;
    TextView textViewName, textViewChangeMode;
    Button buttonSubmit;
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
        editTextName = findViewById(R.id.editTextName);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        textViewChangeMode = findViewById(R.id.textViewChangeMode);
        textViewName = findViewById(R.id.textViewName);
        textViewChangeMode.setOnClickListener(this::changeMode);
        buttonSubmit.setOnClickListener(this::submit);
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
                    String msg = Tools.handleErrorResponse(LoginActivity.this,response);
                    Tools.showToast(t, LoginActivity.this, msg);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {
                throwable.printStackTrace();
                Tools.showToast(t,LoginActivity.this,R.string.something_happened);
            }
        };
    }

    private void submit(View view) {
        if (buttonSubmit.getText().equals(getString(R.string.register))){
            register();
        }else{
            login();
        }
    }

    private void register() {
        RegisterRequest request = new RegisterRequest(editTextUsername.getText().toString(),
                                                      editTextPassword.getText().toString(),
                                                      editTextName.getText().toString());
        Call<JsonObject> call = apiInterface.register(request);
        call.enqueue(handleResponse);
    }

    private void login() {
        LoginRequest request = new LoginRequest(editTextUsername.getText().toString(),
                                                editTextPassword.getText().toString());
        Call<JsonObject> call = apiInterface.login(request);
        call.enqueue(handleResponse);
    }

    private void changeMode(View view) {
        if (textViewChangeMode.getText().equals(getString(R.string.register))){
            registerUI();
        }else{
            loginUI();
        }
    }

    private void registerUI() {
        textViewName.setVisibility(View.VISIBLE);
        editTextName.setVisibility(View.VISIBLE);
        textViewChangeMode.setText(getString(R.string.login));
        buttonSubmit.setText(getString(R.string.register));
    }

    private void loginUI() {
        textViewName.setVisibility(View.GONE);
        editTextName.setVisibility(View.GONE);
        textViewChangeMode.setText(getString(R.string.register));
        buttonSubmit.setText(getString(R.string.login));
    }

    private void openMainActivity(){
        startActivity(new Intent(LoginActivity.this,MainActivity.class));
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}