package com.example.trinity.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trinity.databinding.MangadexDialogItemLayoutBinding;
import com.example.trinity.valueObject.TagManga;
import com.google.gson.JsonArray;

import java.util.ArrayList;

public class MangaDexTagsAdapter extends RecyclerView.Adapter<MangaDexTagsAdapter.TagsViewHolder> {
    private Context context;
    private ArrayList<TagManga> tags;
    private ArrayList<String> selectedTags;

    public MangaDexTagsAdapter(@NonNull Context context,@NonNull ArrayList<TagManga> tags,@NonNull ArrayList<String> selectedTags) {
        this.context = context;
        this.tags = tags;
        this.selectedTags = selectedTags;

    }

    @NonNull
    @Override
    public TagsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TagsViewHolder(MangadexDialogItemLayoutBinding.inflate(LayoutInflater.from(context),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull TagsViewHolder holder, int position) {
        holder.binding.tagTitle.setText(tags.get(holder.getAdapterPosition()).getNome());
        holder.binding.tagTitle.setTag(tags.get(holder.getAdapterPosition()).getId());
        for(String s: selectedTags){
            if(s.equals(tags.get(holder.getAdapterPosition()).getId())){
                holder.binding.tagTitle.setChecked(true);
                break;
            }
        }
        holder.binding.tagTitle.setOnCheckedChangeListener((v,b)->{
            if(b){
                selectedTags.add((String) holder.binding.tagTitle.getTag());
                System.out.println(selectedTags);
            }else{
                for(String s : selectedTags){
                    if(s.equals((String) holder.binding.tagTitle.getTag())){
                        selectedTags.remove(s);
                        break;
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    public static class TagsViewHolder extends RecyclerView.ViewHolder{

        MangadexDialogItemLayoutBinding binding;

        public TagsViewHolder(@NonNull MangadexDialogItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
