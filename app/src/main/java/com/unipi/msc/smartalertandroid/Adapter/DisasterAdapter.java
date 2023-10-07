package com.unipi.msc.smartalertandroid.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.msc.smartalertandroid.Model.Disaster;
import com.unipi.msc.smartalertandroid.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisasterAdapter extends RecyclerView.Adapter<DisasterAdapter.ViewHolder>{
    List<Disaster> disasters;
    Map<Long, Boolean> disasterChecked = new HashMap<>();
    public DisasterAdapter(List<Disaster> disasters) {
        this.disasters = disasters;
    }
    @NonNull
    @Override
    public DisasterAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.disaster_layout, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull DisasterAdapter.ViewHolder holder, int position) {
        holder.switchCompat.setText(disasters.get(position).getName());
        holder.switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> disasterChecked.put(disasters.get(position).getId(),isChecked));
        holder.switchCompat.setChecked(true);
    }
    @Override
    public int getItemCount() {
        return disasters.size();
    }
    public List<Long> getSelectedDisasters(){
        List<Long> selectedDisaster = new ArrayList<>();
        disasterChecked.forEach((disastersId, isChecked) -> {
            if (isChecked) {
                selectedDisaster.add(disastersId);
            }
        });
        return selectedDisaster;
    }

    public void reset() {
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        SwitchCompat switchCompat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            switchCompat = itemView.findViewById(R.id.switchCompatDisaster);
        }
    }
}
