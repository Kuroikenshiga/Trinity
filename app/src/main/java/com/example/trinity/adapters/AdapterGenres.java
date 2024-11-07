package com.example.trinity.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trinity.databinding.GenresItemLayoutBinding;
import com.example.trinity.valueObject.TagManga;

import java.util.ArrayList;

public class AdapterGenres extends RecyclerView.Adapter<AdapterGenres.GenresViewHolder> {

    private Context context;
    private ArrayList<TagManga> tagMangas;

    public AdapterGenres(Context c,@NonNull ArrayList<TagManga> array){
        this.context = c;
        this.tagMangas = array;
    }

    @NonNull
    @Override
    public GenresViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GenresItemLayoutBinding genresViewBinding = GenresItemLayoutBinding.inflate(LayoutInflater.from(context),parent,false);

        return new GenresViewHolder(genresViewBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull GenresViewHolder holder, @SuppressLint("RecyclerView") int position) {
        //System.out.println(tagMangas.get(position).getNome());
        holder.binding.textGenres.setText(tagMangas.get(position).getNome());
        holder.binding.genresItemContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // System.out.println("Clicado: "+tagMangas.get(position).getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.tagMangas.size();
    }

    public static class GenresViewHolder extends RecyclerView.ViewHolder{
        GenresItemLayoutBinding binding ;
        public GenresViewHolder(@NonNull GenresItemLayoutBinding binding){

            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
