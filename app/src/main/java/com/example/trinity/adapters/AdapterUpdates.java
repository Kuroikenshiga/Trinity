package com.example.trinity.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.trinity.Interfeces.Extensions;
import com.example.trinity.MainActivity;

import com.example.trinity.MangaShowContentActivity;
import com.example.trinity.R;
import com.example.trinity.databinding.DateGroupItemLayoutBinding;
import com.example.trinity.databinding.UpdatesItemLayoutBinding;
import com.example.trinity.extensions.MangaDexExtension;
import com.example.trinity.extensions.MangakakalotExtension;
import com.example.trinity.fragments.UpdatesFragment;
import com.example.trinity.models.Model;
import com.example.trinity.storageAcess.LogoMangaStorage;
import com.example.trinity.valueObject.ChapterManga;
import com.example.trinity.valueObject.ChapterUpdated;
import com.example.trinity.viewModel.MangaDataViewModel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AdapterUpdates extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<ChapterUpdated> chapterUpdateds;
    MangaDataViewModel mangaDataViewModel;
    Fragment fragment;
    LogoMangaStorage storage;
    private String time = "";
    private static int VIEW_TYPE_UPDATE_ITEM = 1, VIEW_TYPE_DATE_GROUP = 2;

    public AdapterUpdates(@NonNull Context context, ArrayList<ChapterUpdated> chaps, Fragment fragment1) {
        this.context = context;
        this.chapterUpdateds = chaps;
        storage = new LogoMangaStorage(context);
        this.fragment = fragment1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UpdatesItemLayoutBinding binding = UpdatesItemLayoutBinding.inflate(LayoutInflater.from(this.context), parent, false);
        DateGroupItemLayoutBinding bindingDateGroup = DateGroupItemLayoutBinding.inflate(LayoutInflater.from(context), parent, false);
        return viewType == VIEW_TYPE_UPDATE_ITEM ? new UpdatesViewHolder(binding) : new DateGroupViewHolder(bindingDateGroup);
    }

    @Override
    public int getItemViewType(int position) {
        return chapterUpdateds.get(position) == null ? VIEW_TYPE_DATE_GROUP : VIEW_TYPE_UPDATE_ITEM;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        System.out.println(chapterUpdateds.get(holder.getAdapterPosition()));
        if (holder instanceof UpdatesViewHolder) {

            if ((fragment).getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                Glide.with(fragment)
                        .load(storage.getLogoFromStorage(this.chapterUpdateds.get(holder.getAdapterPosition()).getManga().getId()))
                        .override((int) context.getResources().getDisplayMetrics().density * 50, (int) context.getResources().getDisplayMetrics().density * 50)
                        .into(((UpdatesViewHolder)holder).binding.mangaCover);

                ((UpdatesViewHolder)holder).binding.chapterTitle.setText(new StringBuilder().append(this.chapterUpdateds.get(holder.getAdapterPosition()).getChapterManga().getChapter()).append(" CH. ").append(this.chapterUpdateds.get(holder.getAdapterPosition()).getManga().getTitulo()).toString());

                Calendar mangaDate = Calendar.getInstance();
                Instant instant = Instant.parse(this.chapterUpdateds.get(holder.getAdapterPosition()).getChapterManga().getDateRFC3339());
                Date dateDate = new Date(instant.toEpochMilli());
                mangaDate.setTime(dateDate);
                this.chapterUpdateds.get(holder.getAdapterPosition()).getChapterManga().setData(mangaDate);

                TypedValue typedValue = new TypedValue();
                context.getTheme().resolveAttribute(this.chapterUpdateds.get(holder.getAdapterPosition()).getChapterManga().isAlredyRead()?com.google.android.material.R.attr.colorPrimary:com.google.android.material.R.attr.colorTertiary,typedValue,true);
                ((UpdatesViewHolder)holder).binding.chapterTitle.setTextColor(typedValue.data);
                ((UpdatesViewHolder)holder).binding.mangaCover.setOnClickListener((v)->{
                    Intent intent = new Intent(context, MangaShowContentActivity.class);
                    intent.putExtra("Item",(Parcelable)chapterUpdateds.get(holder.getAdapterPosition()).getManga());
                    intent.putExtra("Extension",chapterUpdateds.get(holder.getAdapterPosition()).getManga().getId().contains("kakalot")||chapterUpdateds.get(holder.getAdapterPosition()).getManga().getId().contains("manganato")?Extensions.MANGAKAKALOT:Extensions.MANGADEX);
                    context.startActivity(intent);
                });
                ((UpdatesViewHolder)holder).binding.chapterTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Model model = Model.getInstance(context);

                        new Thread() {
                            @Override
                            public void run() {
//                                ArrayList<ChapterManga>allChapterByMangaID = model.getAllChapterByMangaID(chapterUpdateds.get(holder.getAdapterPosition()).getManga().getId(), chapterUpdateds.get(holder.getAdapterPosition()).getManga().getLanguage());
                                ((Activity)context).runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {


                                        Intent intent = new Intent(context, MangaShowContentActivity.class);
                                        intent.putExtra("Item",(Parcelable)chapterUpdateds.get(holder.getAdapterPosition()).getManga());
                                        intent.putExtra("route",MangaShowContentActivity.TO_LOADING);
                                        intent.putExtra("idChap",chapterUpdateds.get(holder.getAdapterPosition()).getChapterManga().getId());
                                        intent.putExtra("Extension",chapterUpdateds.get(holder.getAdapterPosition()).getManga().getId().contains("kakalot")||chapterUpdateds.get(holder.getAdapterPosition()).getManga().getId().contains("manganato")?Extensions.MANGAKAKALOT:Extensions.MANGADEX);
                                        context.startActivity(intent);

                                    }
                                });

                                if (model.chapterRead(chapterUpdateds.get(holder.getAdapterPosition()).getChapterManga())) {
                                    MainActivity mangaShowContentActivity = (MainActivity) context;
                                    chapterUpdateds.get(holder.getAdapterPosition()).getChapterManga().setAlredyRead(true);
                                    mangaShowContentActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            TypedValue value = new TypedValue();
                                            context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary,value,false);
                                            ((UpdatesViewHolder)holder).binding.chapterTitle.setTextColor(value.data);
                                        }
                                    });
                                }
                            }
                        }.start();

                    }
                });
            }

        }
        else{
            if(position != chapterUpdateds.size() - 1){
                ((DateGroupViewHolder)holder).binding.dateGroup.setText(chapterUpdateds.get(position+1).getChapterManga().returnTimeReleased());
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.chapterUpdateds.size();
    }

    public static class UpdatesViewHolder extends RecyclerView.ViewHolder {
        UpdatesItemLayoutBinding binding;

        public UpdatesViewHolder(UpdatesItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class DateGroupViewHolder extends RecyclerView.ViewHolder {
        DateGroupItemLayoutBinding binding;

        public DateGroupViewHolder(DateGroupItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public MangaDataViewModel getMangaDataViewModel() {
        return mangaDataViewModel;
    }

    public void setMangaDataViewModel(MangaDataViewModel mangaDataViewModel) {
        this.mangaDataViewModel = mangaDataViewModel;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

//    public static void bubbleSortDataSet(ArrayList<ChapterUpdated>chapterUpdateds){
//
//        boolean dataSetChanged = true;
//        while(dataSetChanged){
//            dataSetChanged = false;
//            for(int i = 0;i < chapterUpdateds.size()-1;i++){
//                Instant currentInstantUpdated = Instant.parse(chapterUpdateds.get(i).getChapterManga().getDateRFC3339());
//                Instant  nextInstantUpdated = Instant.parse(chapterUpdateds.get(i+1).getChapterManga().getDateRFC3339());
//                if(currentInstantUpdated.getEpochSecond() > nextInstantUpdated.getEpochSecond()){
//                    ChapterUpdated auxChapterUpdated = chapterUpdateds.get(i+1);
//                    chapterUpdateds.set(i+1,chapterUpdateds.get(i));
//                    chapterUpdateds.set(i,auxChapterUpdated);
//                    dataSetChanged = true;
//                }
//            }
//        }
//
//    }
}
