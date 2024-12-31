package com.example.trinity.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
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

    private int limiter;
    //Altere o valor dessa constante, caso queira aumentar a quantidade de itens que podem ser inseridos em cada novo "load" de dados
    private static final int LIMITE_PER_LOAD = 100;
    //Altere o valor dessa constante, caso queira aumentar a quantidade mínima de itens a cada "load"
    private static final int MIN_ITENS_PER_LOAD = 50;

    public AdapterChapters(Context context, @NonNull ArrayList<ChapterManga> chapters) {
        this.context = context;
        this.chapters = chapters;
        chapterToDownload = new ArrayList<>();
        limiter = chapters.size() < LIMITE_PER_LOAD?0:chapters.size()/LIMITE_PER_LOAD*LIMITE_PER_LOAD;
        limiter -= chapters.size() - limiter < MIN_ITENS_PER_LOAD && limiter > MIN_ITENS_PER_LOAD?MIN_ITENS_PER_LOAD:0;
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChapterViewHolder(ChapterItemLayoutBinding.inflate(LayoutInflater.from(this.context), parent, false));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Calendar mangaDate = Calendar.getInstance();
        Instant instant = Instant.parse(this.chapters.get(position).getDateRFC3339());
        Date dateDate = new Date(instant.toEpochMilli());
        mangaDate.setTime(dateDate);
        this.chapters.get(position).setData(mangaDate);
        holder.binding.chapNumber.setText("Ch. " + this.chapters.get(position).getChapter() + " - " + this.chapters.get(position).getTitle());

        holder.binding.chapDate.setText(chapters.get(position).returnTimeReleased() +" - "+ this.chapters.get(position).getScan());
        holder.binding.downloaded.setVisibility(chapters.get(position).isDownloaded()?View.VISIBLE:View.GONE);

        TypedValue typedValuePrimary = new TypedValue();


        context.getTheme().resolveAttribute(this.chapters.get(position).isAlredyRead() ?androidx.appcompat.R.attr.colorPrimary:com.google.android.material.R.attr.colorTertiary,typedValuePrimary,true);
//        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorTertiary,typedValueSecondary,true);

        holder.binding.chapNumber.setTextColor(typedValuePrimary.data);

        TypedValue typedValuePrimaryBc = new TypedValue();


        context.getTheme().resolveAttribute(chapters.get(position).isSelected?com.google.android.material.R.attr.colorSecondary:com.google.android.material.R.attr.colorSurface,typedValuePrimaryBc,true);
//        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurface,typedValueSecondaryBc,true);
//        holder.binding.chapter.setBackground(chapters.get(position).isSelected?ResourcesCompat.getDrawable(context.getResources(),R.color.FullBlack,context.getTheme()):ResourcesCompat.getDrawable(context.getResources(),R.color.BackGroundScreen,context.getTheme()));
        holder.binding.chapter.setBackgroundColor(typedValuePrimaryBc.data);
        boolean value = chapters.get(position).isSelected;
        if (chapters.get(position).getCurrentPage() > 0) {
            holder.binding.currentPage.setText(String.format("Última página: %d", (chapters.get(position).getCurrentPage() + 1)));
        }

        GestureDetector gestureDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e){

                if(isLongPressed){
                    TypedValue typedValue = new TypedValue();
                    if(!chapters.get(position).isSelected){
                        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary,typedValue,false);
                        chapters.get(position).isSelected = true;
                        chapterToDownload.add(chapters.get(position));
//
                    }
                    else{
                        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurface,typedValue,false);
                        chapters.get(position).isSelected = false;
                        removeChapterFromDownload(chapters.get(position).getId());
//
                    }
                    holder.binding.chapter.setBackgroundColor(typedValue.data);
                    return true;
                }
                TypedValue typedValueText = new TypedValue();
                context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary,typedValueText,false);
                holder.binding.chapNumber.setTextColor(typedValueText.data);
                new Thread() {
                    @Override
                    public void run() {

                        Model model = Model.getInstance(context);
                        model.chapterRead(chapters.get(position));

                    }
                }.start();
                if (mangaDataViewModel != null) {
                    mangaDataViewModel.setIdChap(chapters.get(holder.getAdapterPosition()).getId());
                    mangaDataViewModel.getManga().getChapters().get(holder.getAdapterPosition()).setAlredyRead(true);
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
//                Drawable drawable = ResourcesCompat.getDrawable(context.getResources(),R.color.FullBlack,context.getTheme());
                TypedValue typedValue = new TypedValue();
                context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary,typedValue,false);
                holder.binding.chapter.setBackgroundColor(typedValue.data);
//                holder.binding.chapter.post(()->{holder.binding.getRoot().setBackground(drawable);});
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
    public void onViewAttachedToWindow(@NonNull ChapterViewHolder holder) {
        super.onViewRecycled(holder);


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

    private void onReachEndDataSet(){
        if(limiter == 0)return;
        int itensInserted = 0;
        if(limiter >= LIMITE_PER_LOAD){
            itensInserted = LIMITE_PER_LOAD;
            limiter -= LIMITE_PER_LOAD;
        }
        else{
            itensInserted = limiter;
            limiter = 0;
        }

        this.notifyItemRangeInserted(chapters.size()-limiter-itensInserted,itensInserted);
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
