package com.unipi.msc.smartalertandroid.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.unipi.msc.smartalertandroid.Model.Risk;
import com.unipi.msc.smartalertandroid.R;

import java.util.Collections;
import java.util.List;

public class RiskArrayAdapter extends ArrayAdapter<List<Risk>> {
    private List<Risk> riskList;
    private Context context;
    public RiskArrayAdapter(@NonNull Context context, List<Risk> riskList) {
        super(context, 0, Collections.singletonList(riskList));
        this.riskList = riskList;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // convertView which is recyclable view
        View currentItemView = convertView;

        // of the recyclable view is null then inflate the custom layout for the same
        if (currentItemView == null) {
            currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.custom_list_view, parent, false);
        }

        // then according to the position of the view assign the desired TextView 2 for the same
        TextView textView = currentItemView.findViewById(R.id.textView);
        textView.setText(riskList.get(position).getName());

        // then return the recyclable view
        return currentItemView;
    }
}
