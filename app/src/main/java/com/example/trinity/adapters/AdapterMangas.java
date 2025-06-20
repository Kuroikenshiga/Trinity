package com.example.trinity.adapters;

import android.annotation.SuppressLint;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.trinity.Interfeces.Extensions;
import com.example.trinity.MainActivity;
import com.example.trinity.MangaShowContentActivity;
import com.example.trinity.R;
import com.example.trinity.databinding.MangaItemLayoutBinding;
import com.example.trinity.databinding.MangaItemLayoutHorizontalViewBinding;
import com.example.trinity.storageAcess.LogoMangaStorage;
import com.example.trinity.storageAcess.LogoMangaStorageTemp;
import com.example.trinity.valueObject.Manga;

import java.io.Serializable;
import java.util.ArrayList;

public class AdapterMangas extends RecyclerView.Adapter<AdapterMangas.MangaViewHolder> {
    private final Context c;
    private String language;
    private boolean showLanguageIcon = false;
    private boolean isShowAmountChapterToRead = false;
    private ArrayList<Manga> mangaArrayList;
    private boolean isHorizontalView = false;
    private LogoMangaStorage storage;
    private LogoMangaStorageTemp storageTemp;
    private boolean isFromUpdates = false;
    private Fragment fragment;


    public AdapterMangas(Context c, ArrayList<Manga> mangaModels) {
        this.c = c;
        this.mangaArrayList = mangaModels;
        this.storage = new LogoMangaStorage(c);
    }
    public AdapterMangas(Context c, ArrayList<Manga> mangaModels,Fragment fragment1) {
        this.c = c;
        this.mangaArrayList = mangaModels;
        this.storage = new LogoMangaStorage(c);
        this.fragment = fragment1;

    }

    public AdapterMangas(Context c, ArrayList<Manga> mangaModels, String language) {
        this.c = c;
        this.language = language;
        this.mangaArrayList = mangaModels;
    }

    @NonNull
    @Override
    public MangaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (isHorizontalView) {
            MangaItemLayoutHorizontalViewBinding mangaItemLayoutHorizontalViewBinding = MangaItemLayoutHorizontalViewBinding.inflate(LayoutInflater.from(c), parent, false);
            return new MangaViewHolder(mangaItemLayoutHorizontalViewBinding);
        }

        MangaItemLayoutBinding mangaItemBinding = MangaItemLayoutBinding.inflate(LayoutInflater.from(c), parent, false);
        return new MangaViewHolder(mangaItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MangaViewHolder holder, @SuppressLint("RecyclerView") int position) {

        if (((AppCompatActivity) (c)).getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            if (isHorizontalView) {

                if(fragment == null){
                    Glide.with(c)
                            .load(!isFromUpdates ? storage.getLogoFromStorage(mangaArrayList.get(holder.getAdapterPosition()).getId()) : storageTemp.getLogoFromTempStorage(mangaArrayList.get(holder.getAdapterPosition()).getId()))
//                    .override((int) c.getResources().getDisplayMetrics().density * 120, (int) c.getResources().getDisplayMetrics().density * 170)
                            .into(holder.horizontalViewBinding.mangaLogo);
                }
                else{
                    Glide.with(fragment)
                            .load(!isFromUpdates ? storage.getLogoFromStorage(mangaArrayList.get(holder.getAdapterPosition()).getId()) : storageTemp.getLogoFromTempStorage(mangaArrayList.get(holder.getAdapterPosition()).getId()))
//                    .override((int) c.getResources().getDisplayMetrics().density * 120, (int) c.getResources().getDisplayMetrics().density * 170)
                            .into(holder.horizontalViewBinding.mangaLogo);
                }

                holder.horizontalViewBinding.title.setText(mangaArrayList.get(holder.getAdapterPosition()).getTitulo());
                holder.horizontalViewBinding.getRoot().setClickable(true);
                if (this.showLanguageIcon) {
                    Glide.with(c)

                            .load(mangaArrayList.get(holder.getAdapterPosition()).getLanguage().equals("pt-br") ? R.drawable.brazil_flag : mangaArrayList.get(holder.getAdapterPosition()).getLanguage().equals("en") ? R.drawable.usa_flag : R.drawable.spain_flag)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .centerInside()
                            .into(holder.horizontalViewBinding.languageIcon);
                }

                holder.horizontalViewBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(c, MangaShowContentActivity.class);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            intent.putExtra("Item", (Parcelable) mangaArrayList.get(holder.getAdapterPosition()));
                        }
                        else{
                            intent.putExtra("Item", (Serializable) mangaArrayList.get(holder.getAdapterPosition()));
                        }
                        intent.putExtra("Extension",mangaArrayList.get(holder.getAdapterPosition()).getId().contains("mangakakalot")||mangaArrayList.get(holder.getAdapterPosition()).getId().contains("chapmanganato")? Extensions.MANGAKAKALOT:Extensions.MANGADEX);
                        intent.putExtra("Language", language == null ? mangaArrayList.get(holder.getAdapterPosition()).getLanguage() : language);
                        intent.putExtra("FromMain", c instanceof MainActivity);
                        c.startActivity(intent);
                    }
                });
                return;
            }

            if(this.fragment == null){
//                System.out.println("Context");
                Glide.with(c).load(!isFromUpdates ? storage.getLogoFromStorage(mangaArrayList.get(holder.getAdapterPosition()).getId()) : storageTemp.getLogoFromTempStorage(mangaArrayList.get(holder.getAdapterPosition()).getId()))
//                    .override((int) c.getResources().getDisplayMetrics().density * 120, (int) c.getResources().getDisplayMetrics().density * 170)
                        .into(holder.binding.mangaLogo);
            }else {
//                System.out.println("Fragment");
                Glide.with(this.fragment).load(!isFromUpdates ? storage.getLogoFromStorage(mangaArrayList.get(holder.getAdapterPosition()).getId()) : storageTemp.getLogoFromTempStorage(mangaArrayList.get(holder.getAdapterPosition()).getId()))
//                    .override((int) c.getResources().getDisplayMetrics().density * 120, (int) c.getResources().getDisplayMetrics().density * 170)
                        .into(holder.binding.mangaLogo);
            }


            holder.binding.title.setText(mangaArrayList.get(holder.getAdapterPosition()).getTitulo());
            holder.binding.getRoot().setClickable(true);
            if (this.showLanguageIcon) {
                if(fragment == null){
//                    System.out.println("Context");
                    Glide.with(c).load(mangaArrayList.get(holder.getAdapterPosition()).getLanguage().equals("pt-br") ? R.drawable.brazil_flag : mangaArrayList.get(holder.getAdapterPosition()).getLanguage().equals("en") ? R.drawable.usa_flag : R.drawable.spain_flag)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .override((int) (30 * c.getResources().getDisplayMetrics().density), (int) (30 * c.getResources().getDisplayMetrics().density))
                            .into(holder.binding.languageIcon);
                }
                else{
//                    System.out.println("Fragment");
                    Glide.with(fragment).load(mangaArrayList.get(holder.getAdapterPosition()).getLanguage().equals("pt-br") ? R.drawable.brazil_flag : mangaArrayList.get(holder.getAdapterPosition()).getLanguage().equals("en") ? R.drawable.usa_flag : R.drawable.spain_flag)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .override((int) (30 * c.getResources().getDisplayMetrics().density), (int) (30 * c.getResources().getDisplayMetrics().density))
                            .into(holder.binding.languageIcon);
                }
            }

            holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(c, MangaShowContentActivity.class);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        intent.putExtra("Item", (Parcelable) mangaArrayList.get(holder.getAdapterPosition()));
                    }else{
                        intent.putExtra("Item", (Serializable) mangaArrayList.get(holder.getAdapterPosition()));
                    }
                    intent.putExtra("Extension",mangaArrayList.get(holder.getAdapterPosition()).getId().contains("mangakakalot")||mangaArrayList.get(holder.getAdapterPosition()).getId().contains("chapmanganato")? Extensions.MANGAKAKALOT:Extensions.MANGADEX);
                    intent.putExtra("Language", language == null ? mangaArrayList.get(holder.getAdapterPosition()).getLanguage() : language);
                    intent.putExtra("FromMain", c instanceof MainActivity);
                    c.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mangaArrayList.size();
    }

    public static class MangaViewHolder extends RecyclerView.ViewHolder {
        MangaItemLayoutBinding binding;
        MangaItemLayoutHorizontalViewBinding horizontalViewBinding;

        public MangaViewHolder(@NonNull MangaItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public MangaViewHolder(@NonNull MangaItemLayoutHorizontalViewBinding binding) {
            super(binding.getRoot());
            this.horizontalViewBinding = binding;
        }
    }
    public void setDataSet(ArrayList<Manga> dataSet){
        this.mangaArrayList = dataSet;
        notifyDataSetChanged();
    }
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isShowLanguageIcon() {
        return showLanguageIcon;
    }

    public AdapterMangas setShowLanguageIcon(boolean showLanguageIcon) {
        this.showLanguageIcon = showLanguageIcon;
        return this;
    }

    public void setHorizontalView(boolean horizontalView) {
        isHorizontalView = horizontalView;
    }

    public boolean isShowAmountChapterToRead() {
        return isShowAmountChapterToRead;
    }

    public AdapterMangas setShowAmountChapterToRead(boolean showAmountChapterToRead) {
        isShowAmountChapterToRead = showAmountChapterToRead;
        return this;
    }

    public void setFromUpdates(boolean fromUpdates) {
        isFromUpdates = fromUpdates;
        if (fromUpdates) this.storageTemp = new LogoMangaStorageTemp(c);
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }


}
