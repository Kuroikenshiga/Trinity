package com.example.trinity.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.trinity.databinding.SearchItemLayoutBinding;
import com.example.trinity.storageAcess.LogoMangaStorage;
import com.example.trinity.valueObject.Manga;

import java.util.ArrayList;

public class AdapterSearchItensResult extends RecyclerView.Adapter<AdapterSearchItensResult.SearchItemResultViewHolder> {

    private ArrayList<Manga> resultSet;
    private Context context;
    private LogoMangaStorage storage;

    public AdapterSearchItensResult(ArrayList<Manga> resultSet,Context context) {
        this.resultSet = resultSet;
        this.context = context;
        storage = new LogoMangaStorage(context);
    }
    public void setData(ArrayList<Manga> resultSet){
        this.resultSet = resultSet;
    }
    @NonNull
    @Override
    public SearchItemResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchItemResultViewHolder(SearchItemLayoutBinding.inflate(LayoutInflater.from(context),parent,false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SearchItemResultViewHolder holder, int position) {

        Glide.with(this.context)
                .load(storage.getLogoFromStorage(resultSet.get(holder.getAdapterPosition()).getId()))
                .override((int) context.getResources().getDisplayMetrics().density * 50, (int) context.getResources().getDisplayMetrics().density * 50)
                .into(holder.binding.imageItem);
        holder.binding.mangaTitle.setText(resultSet.get(holder.getAdapterPosition()).getTitulo());
        holder.binding.chapter.setText(Integer.toString(resultSet.get(holder.getAdapterPosition()).getChapters().size()));


    }

    @Override
    public int getItemCount() {
        return this.resultSet.size();
    }

    public static class SearchItemResultViewHolder extends RecyclerView.ViewHolder{
        SearchItemLayoutBinding binding;
        public SearchItemResultViewHolder(@NonNull SearchItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }
}
