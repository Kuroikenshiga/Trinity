package com.example.trinity.adapters.innerAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trinity.databinding.InnerItemSearchSimpleLayoutBinding;

public class InnerAdapterSearchItemPager extends RecyclerView.Adapter<InnerAdapterSearchItemPager.InnerSearchItemPagerViewHolder>{

    private Context context;
    private String[] data;
    private AppCompatEditText editText;

    public InnerAdapterSearchItemPager(Context context, String[] data, AppCompatEditText editText) {
        this.context = context;
        this.data = data;
        this.editText = editText;
    }

    @NonNull
    @Override
    public InnerSearchItemPagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InnerSearchItemPagerViewHolder(InnerItemSearchSimpleLayoutBinding.inflate(LayoutInflater.from(context),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull InnerSearchItemPagerViewHolder holder, int position) {
        if(data[holder.getAdapterPosition()] != null){
            holder.binding.mangaTitle.setText(data[holder.getAdapterPosition()]);
            holder.binding.getRoot().setOnClickListener((v)->{
                this.editText.setText(data[holder.getAdapterPosition()]);
            });
        }

    }

    @Override
    public int getItemCount() {
        return data[0] == null || data[1] == null?1:2;
    }

    public static class InnerSearchItemPagerViewHolder extends RecyclerView.ViewHolder {

        public InnerItemSearchSimpleLayoutBinding binding;

        public InnerSearchItemPagerViewHolder(@NonNull InnerItemSearchSimpleLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
