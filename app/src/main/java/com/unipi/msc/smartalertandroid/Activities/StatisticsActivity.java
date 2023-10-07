package com.unipi.msc.smartalertandroid.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.unipi.msc.smartalertandroid.Adapter.DisasterAdapter;
import com.unipi.msc.smartalertandroid.Model.Disaster;
import com.unipi.msc.smartalertandroid.R;
import com.unipi.msc.smartalertandroid.Retrofit.APIInterface;
import com.unipi.msc.smartalertandroid.Retrofit.RetrofitClient;
import com.unipi.msc.smartalertandroid.Shared.Tools;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsActivity extends AppCompatActivity {

    PieChart pieChart;
    ImageButton imageButtonBack;
    LinearLayout linearLayoutDate;
    APIInterface apiInterface;
    List<Disaster> disasterList = new ArrayList<>();
    Toast t;
    List<Long> disasters = new ArrayList<>();
    Long dateFrom = 0L, dateTo = 0L;
    TextView textViewDate;
    RecyclerView recyclerView;
    DisasterAdapter disasterAdapter;
    Button buttonSearch, buttonClear;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        initViews();
        loadDisasters();
        loadStatistics();
    }

    private void loadDisasters() {
        apiInterface.getDisasters(Tools.getTokenFromMemory(this)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    try {
                        disasterList.clear();
                        List<String> disasters = new ArrayList<>();
                        for (JsonElement element : response.body().get("data").getAsJsonArray()) {
                            Disaster r = Disaster.buildDisaster(element.getAsJsonObject());
                            disasters.add(r.getName());
                            disasterList.add(r);
                        }
                        disasterAdapter = new DisasterAdapter(disasterList);
                        recyclerView.setAdapter(disasterAdapter);
                    }catch (Exception ignore){}
                }else{
                    String msg = Tools.handleErrorResponse(StatisticsActivity.this,response);
                    Tools.showToast(t, StatisticsActivity.this, msg);
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void loadPieChartData(JsonArray jsonArray) {
        pieChart.clear();
        List<PieEntry> pieEntries = new ArrayList<>();
        for (JsonElement elements:jsonArray) {
            JsonObject jsonObject = elements.getAsJsonObject();
            String disaster = null;
            int count = 0;
            try {
                disaster = jsonObject.get("disaster").getAsString();
            }catch (Exception ignore){}
            try {
                count = jsonObject.get("count").getAsInt();
            }catch (Exception ignore) {}

            pieEntries.add(new PieEntry(count, disaster));
        }
        PieDataSet dataSet = new PieDataSet(pieEntries,null);
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
    }
    private void initViews() {
        pieChart = findViewById(R.id.pieChart);
        recyclerView = findViewById(R.id.recyclerView);
        imageButtonBack = findViewById(R.id.imageButtonBack);
        linearLayoutDate = findViewById(R.id.linearLayoutDate);
        textViewDate = findViewById(R.id.textViewDate);
        buttonSearch = findViewById(R.id.buttonSearch);
        buttonClear = findViewById(R.id.buttonClear);
        buttonClear.setOnClickListener(v->clearFields());
        buttonSearch.setOnClickListener(v->loadStatistics());
        imageButtonBack.setOnClickListener(v->finish());
        linearLayoutDate.setOnClickListener(v->datePickerDialog());
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        apiInterface = RetrofitClient.getInstance().create(APIInterface.class);
    }

    private void clearFields() {
        disasterAdapter.reset();
        textViewDate.setText(getString(R.string.no_date_range_selected));
    }

    private void loadStatistics() {
        if (disasterAdapter != null) disasters = disasterAdapter.getSelectedDisasters();
        apiInterface.disasterStatistics(disasters, dateFrom, dateTo, Tools.getTokenFromMemory(this)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    try {
                        if (response.body().size() == 0) return;
                        loadPieChartData(response.body().get("data").getAsJsonArray());
                    }catch (Exception ignore){}
                }else {
                    String msg = Tools.handleErrorResponse(StatisticsActivity.this,response);
                    Tools.showToast(t, StatisticsActivity.this, msg);
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void datePickerDialog() {
        // Creating a MaterialDatePicker builder for selecting a date range
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText(getString(R.string.select_your_desired_dates));

        // Building the date picker dialog
        MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();
        datePicker.addOnPositiveButtonClickListener(selection -> {

            // Retrieving the selected start and end dates
            dateFrom = selection.first;
            dateTo = selection.second;

            // Formating the selected dates as strings
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String startDateString = sdf.format(new Date(dateFrom));
            String endDateString = sdf.format(new Date(dateTo));

            // Creating the date range string
            String selectedDateRange = startDateString + " - " + endDateString;

            // Displaying the selected date range in the TextView
            textViewDate.setText(selectedDateRange);
        });

        // Showing the date picker dialog
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }
}