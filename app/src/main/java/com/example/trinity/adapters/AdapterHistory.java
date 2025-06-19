package com.example.trinity.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.trinity.Interfeces.Extensions;
import com.example.trinity.MainActivity;
import com.example.trinity.MangaShowContentActivity;
import com.example.trinity.databinding.HistoryItemBinding;
import com.example.trinity.storageAcess.LogoMangaStorage;
import com.example.trinity.valueObject.History;

import java.io.Serializable;
import java.util.ArrayList;

public class AdapterHistory extends RecyclerView.Adapter<AdapterHistory.HistoryViewHoler> {

    private Context context;
    private ArrayList<History> histories;
    private LogoMangaStorage storage;
    private Fragment fragment;
    public AdapterHistory(Context context, ArrayList<History> histories, Fragment fragment) {
        this.context = context;
        this.histories = histories;
        storage = new LogoMangaStorage(context);
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public HistoryViewHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HistoryViewHoler(HistoryItemBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHoler holder, int position) {
        if (fragment.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            Glide.with(fragment)
                    .load(storage.getLogoFromStorage(histories.get(holder.getAdapterPosition()).getManga().getId()))
                    .override((int) context.getResources().getDisplayMetrics().density * 50, (int) context.getResources().getDisplayMetrics().density * 50)
                    .into(holder.binding.mangaImage);

            holder.binding.mangaTitle.setText(histories.get(holder.getAdapterPosition()).getManga().getTitulo());
            holder.binding.lastAcess.setText(histories.get(holder.getAdapterPosition()).returnLastTimeAccessed());
            holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MangaShowContentActivity.class);
                    //Testa a versão do sistema operacional antes de serializar o objeto
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                        intent.putExtra("Item", (Parcelable) histories.get(holder.getAdapterPosition()).getManga());
                    }
                    else{
                        intent.putExtra("Item", (Serializable) histories.get(holder.getAdapterPosition()).getManga());
                    }
                    intent.putExtra("Language", histories.get(holder.getAdapterPosition()).getManga().getLanguage());
                    intent.putExtra("Extension",histories.get(holder.getAdapterPosition()).getManga().getId().contains("manganato")||histories.get(holder.getAdapterPosition()).getManga().getId().contains("mangakakalot")? Extensions.MANGAKAKALOT:Extensions.MANGADEX);
                    intent.putExtra("FromMain", context instanceof MainActivity);
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return histories.size();
    }

    public static class HistoryViewHoler extends RecyclerView.ViewHolder {
        HistoryItemBinding binding;

        public HistoryViewHoler(HistoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
