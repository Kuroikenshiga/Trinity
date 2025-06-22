package com.example.trinity.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trinity.adapters.innerAdapters.InnerAdapterSearchItemPager;
import com.example.trinity.databinding.SearchItemLayoutSimpleLayoutBinding;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class AdapterSearchItensSimpleResult  extends RecyclerView.Adapter<AdapterSearchItensSimpleResult.SearchItemResultViewHolder>{

    private Context context;
    private ArrayList<String[]> data;
    private AppCompatEditText editText;
    public AdapterSearchItensSimpleResult(Context context, ArrayList<String[]> data, AppCompatEditText editText) {
        this.context = context;
        this.data = data;
        this.editText = editText;
    }

    @NonNull
    @Override
    public SearchItemResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchItemResultViewHolder(SearchItemLayoutSimpleLayoutBinding.inflate(LayoutInflater.from(context),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull SearchItemResultViewHolder holder, int position) {

        InnerAdapterSearchItemPager innerAdapterSearchItemPager = new InnerAdapterSearchItemPager(context,data.get(holder.getAdapterPosition()),editText);
        holder.binding.pager.setAdapter(innerAdapterSearchItemPager);

    }
    public void setData(ArrayList<String[]> data){
        this.data = data;
    }
    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class SearchItemResultViewHolder extends RecyclerView.ViewHolder{
        SearchItemLayoutSimpleLayoutBinding binding;

        public SearchItemResultViewHolder(@NonNull SearchItemLayoutSimpleLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
