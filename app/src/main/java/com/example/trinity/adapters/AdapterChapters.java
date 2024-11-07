package com.example.trinity.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;


import com.example.trinity.R;
import com.example.trinity.databinding.ChapterItemLayoutBinding;
import com.example.trinity.fragments.InfoMangaFragment;
import com.example.trinity.models.Model;
import com.example.trinity.valueObject.ChapterManga;
import com.example.trinity.viewModel.MangaDataViewModel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Predicate;

public class AdapterChapters extends RecyclerView.Adapter<AdapterChapters.ChapterViewHolder> {

    private Context context;
    private ArrayList<ChapterManga> chapters;
    private Bitmap mangaBitMapLogo;
    private String idMangaApi;
    private String chapterLanguage;
    private MangaDataViewModel mangaDataViewModel;
    private NavController nav;
    private Fragment fragment;
    private boolean isLongPressed = false;
    private ArrayList<ChapterManga> chapterToDownload;
    public AdapterChapters(Context context, @NonNull ArrayList<ChapterManga> chapters) {
        this.context = context;
        this.chapters = chapters;
        chapterToDownload = new ArrayList<>();
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChapterViewHolder(ChapterItemLayoutBinding.inflate(LayoutInflater.from(this.context), parent, false));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        Calendar mangaDate = Calendar.getInstance();
        Instant instant = Instant.parse(this.chapters.get(position).getDateRFC3339());
        Date dateDate = new Date(instant.toEpochMilli());
        mangaDate.setTime(dateDate);
        this.chapters.get(position).setData(mangaDate);
        holder.binding.chapNumber.setText("Ch. " + this.chapters.get(position).getChapter() + " - " + this.chapters.get(position).getTitle());

        holder.binding.chapDate.setText(chapters.get(position).returnTimeReleased() +" - "+ this.chapters.get(position).getScan());
        holder.binding.downloaded.setVisibility(chapters.get(position).isDownloaded()?View.VISIBLE:View.GONE);
        holder.binding.chapNumber.setTextColor(this.chapters.get(position).isAlredyRead() ? context.getColor(R.color.Blue) : context.getColor(R.color.white));
        holder.binding.chapter.setBackground(chapters.get(position).isSelected?ResourcesCompat.getDrawable(context.getResources(),R.color.FullBlack,context.getTheme()):ResourcesCompat.getDrawable(context.getResources(),R.color.BackGroundScreen,context.getTheme()));
        if (chapters.get(position).getCurrentPage() > 0) {
            holder.binding.currentPage.setText(String.format("Última página: %d", (chapters.get(position).getCurrentPage() + 1)));
        }

        GestureDetector gestureDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e){

                if(isLongPressed){

                    Drawable drawable;
                    if(!chapters.get(position).isSelected){
                        drawable =  ResourcesCompat.getDrawable(context.getResources(),R.color.FullBlack,context.getTheme());
                        chapters.get(position).isSelected = true;
                        chapterToDownload.add(chapters.get(position));
//                        System.out.println(chapterToDownload.size());
                    }
                    else{
                        drawable = ResourcesCompat.getDrawable(context.getResources(),R.color.BackGroundScreen,context.getTheme());
                        chapters.get(position).isSelected = false;
                        removeChapterFromDownload(chapters.get(position).getId());
//                        System.out.println(chapterToDownload.size());
                    }
                    holder.binding.chapter.setBackground(drawable);
                    return true;
                }

                new Thread() {
                    @Override
                    public void run() {
                        holder.binding.chapNumber.setTextColor(context.getColor(R.color.Blue));
                        Model model = Model.getInstance(context);
                        model.chapterRead(chapters.get(position));

                    }
                }.start();
                if (mangaDataViewModel != null) {
                    mangaDataViewModel.setIdChap(chapters.get(position).getId());
                }
                InfoMangaFragment f = (InfoMangaFragment) fragment;
                Bundle bundle = new Bundle();
                bundle.putInt("currentPage", chapters.get(position).getCurrentPage());
                bundle.putBoolean("isMangaAdded",f.isMangaAdded());
                f.navigateToRead(bundle);
                return true;
            }
            @Override
            public void onLongPress(MotionEvent e){
                if(isLongPressed)return;
                isLongPressed = true;
                holder.isSelected = true;
                InfoMangaFragment f = (InfoMangaFragment) fragment;
                f.controlDownloadButtonVisibility(true);
                Drawable drawable = ResourcesCompat.getDrawable(context.getResources(),R.color.FullBlack,context.getTheme());
                holder.binding.chapter.setBackground(drawable);
                chapterToDownload.add(chapters.get(position));
            }

        });

        holder.binding.getRoot().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
//        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new Thread() {
//                    @Override
//                    public void run() {
//                        holder.binding.chapNumber.setTextColor(context.getColor(R.color.Blue));
//                        Model model = new Model(context);
//                        model.chapterRead(chapters.get(position));
//
//                    }
//                }.start();
//                if (mangaDataViewModel != null) {
//                    mangaDataViewModel.setIdChap(chapters.get(position).getId());
//                }
//                InfoMangaFragment f = (InfoMangaFragment) fragment;
//                Bundle bundle = new Bundle();
//                bundle.putInt("currentPage", chapters.get(position).getCurrentPage());
//                f.navigateToRead(bundle);
//
//
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return this.chapters.size();
    }

    public class ChapterViewHolder extends RecyclerView.ViewHolder {

        public ChapterItemLayoutBinding binding;
        public boolean isSelected = false;
        public ChapterViewHolder(ChapterItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void setMangaBitMapLogo(Bitmap mangaBitMapLogo) {
        this.mangaBitMapLogo = mangaBitMapLogo;
    }

    public String getIdMangaApi() {
        return idMangaApi;
    }

    public void setIdMangaApi(String idMangaApi) {
        this.idMangaApi = idMangaApi;
    }

    public String getChapterLanguage() {
        return chapterLanguage;
    }

    public void setChapterLanguage(String chapterLanguage) {
        this.chapterLanguage = chapterLanguage;
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

    public void disablelongPressedItens(){
        this.isLongPressed = false;

        for(ChapterManga ch:chapterToDownload){
            for(int i = 0; i < chapters.size();i++){
                if(ch.getId().equals(chapters.get(i).getId())){
                    this.chapters.get(i).isSelected = false;
                    this.notifyItemChanged(i);
                }
            }
        }
        this.chapterToDownload.clear();
    }

    public void setDownloadSucecessful(String[] idMangaApi){
        disablelongPressedItens();
        if(idMangaApi == null)return;
        for(String ch:idMangaApi){
            for(int i = 0; i < chapters.size();i++){
                if(ch.equals(chapters.get(i).getId())){
                    this.chapters.get(i).setDownloaded(true);
                    this.notifyItemChanged(i);
                    InfoMangaFragment f = (InfoMangaFragment) fragment;
                    f.controlDownloadButtonVisibility(false);

                }
            }
        }

        isLongPressed = false;
    }

    private void removeChapterFromDownload(String idChapterApi){
        this.chapterToDownload.removeIf(ch -> ch.getId().equals(idChapterApi));
        if(this.chapterToDownload.isEmpty()){
            InfoMangaFragment f = (InfoMangaFragment) fragment;
            f.controlDownloadButtonVisibility(false);
            this.isLongPressed = false;
        }
    }
    public ArrayList<ChapterManga> getChapterToDownload(){
        return this.chapterToDownload;
    }
    public ArrayList<ChapterManga> getDataSet(){
        return this.chapters;
    }
}
