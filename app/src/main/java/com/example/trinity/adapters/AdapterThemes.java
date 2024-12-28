package com.example.trinity.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trinity.databinding.ThemeItemLayoutBinding;

import java.util.ArrayList;

public class AdapterThemes extends RecyclerView.Adapter<AdapterThemes.ThemeViewHolder>{
    public static final String PRIMARY_COLOR = "colorPrimary";
    public static final String SECONDARY_COLOR = "colorSecondary";
    public static final String SURFACE_COLOR = "colorSurface";
    public static final String TERTIARY_COLOR = "colorTertiary";
    private ArrayList<ContentValues> contentValues;
    private Context context;

    public AdapterThemes(ArrayList<ContentValues> contentValues, Context context) {
        this.contentValues = contentValues;
        this.context = context;
    }

    @NonNull
    @Override
    public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ThemeViewHolder(ThemeItemLayoutBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeViewHolder holder, int position) {

        holder.binding.primary.setBackgroundColor(contentValues.get(holder.getAdapterPosition()).getAsInteger(PRIMARY_COLOR));
        holder.binding.secondary.setBackgroundColor(contentValues.get(holder.getAdapterPosition()).getAsInteger(SECONDARY_COLOR));
        holder.binding.surface.setBackgroundColor(contentValues.get(holder.getAdapterPosition()).getAsInteger(SURFACE_COLOR));
        holder.binding.tertiary.setBackgroundColor(contentValues.get(holder.getAdapterPosition()).getAsInteger(TERTIARY_COLOR));

    }

    @Override
    public int getItemCount() {
        return contentValues.size();
    }

    public static class ThemeViewHolder extends RecyclerView.ViewHolder{

        public ThemeItemLayoutBinding binding;
        public ThemeViewHolder(@NonNull ThemeItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
