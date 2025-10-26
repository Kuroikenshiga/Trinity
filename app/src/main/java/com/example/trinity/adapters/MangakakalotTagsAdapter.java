package com.example.trinity.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trinity.databinding.MangakakalotTagItemLayoutBinding;
import com.example.trinity.valueObject.TagManga;

import java.util.ArrayList;

public class MangakakalotTagsAdapter extends RecyclerView.Adapter<MangakakalotTagsAdapter.TagsViewHolder>{

    private Context context;
    private ArrayList<TagManga> tags;
    private Runnable runnable;
    private String genre = "";
    public MangakakalotTagsAdapter(Context context, ArrayList<TagManga> tags,Runnable runnable) {
        this.context = context;
        this.tags = tags;
        this.runnable = runnable;
    }

    @NonNull
    @Override
    public TagsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TagsViewHolder(MangakakalotTagItemLayoutBinding.inflate(LayoutInflater.from(context),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull TagsViewHolder holder, int position) {
        holder.binding.tagTitle.setText(tags.get(holder.getAdapterPosition()).getNome());
        holder.binding.tagTitle.setOnClickListener((v)->{
            this.genre = tags.get(holder.getAdapterPosition()).getNome();
            runnable.run();
        });
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }
    public String getTagName(){
        return this.genre;
    }
    public static class TagsViewHolder extends RecyclerView.ViewHolder{
        MangakakalotTagItemLayoutBinding binding;
        public TagsViewHolder(@NonNull MangakakalotTagItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
