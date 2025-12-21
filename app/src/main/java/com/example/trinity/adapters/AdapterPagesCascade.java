package com.example.trinity.adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.example.trinity.R;
import com.example.trinity.databinding.EndReadItemBinding;
import com.example.trinity.databinding.PageCascadeItemBinding;
import com.example.trinity.databinding.StartReadItemBinding;
import com.example.trinity.fragments.ReaderMangaFragment;
import com.example.trinity.storageAcess.LogoMangaStorage;
import com.example.trinity.storageAcess.LogoMangaStorageTemp;
import com.example.trinity.valueObject.ChapterManga;

import java.util.ArrayList;

public class AdapterPagesCascade extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<String> imagesResource;
    private Fragment fragment;
    private float currentXTouch;
    private float currentYTouch;
    private String LogoManga;
    private String timeWasteString;
    private boolean isLastChapter = false;
    private boolean isFirstChapter = false;
    private float screenWidth,screenHeight;
    private GestureDetector gestureDetector;
    public ChapterManga[] previousCurrentAndNextChapter = new ChapterManga[3];
    public static int VIEW_TYPE_HEADER = 0;
    public static int VIEW_TYPE_ITEM = 1;
    public static int VIEW_TYPE_FOOTER = 2;

    private LogoMangaStorage logoMangaStorage;
    private LogoMangaStorageTemp logoMangaStorageTemp;
    private int numPagesIgnored = 0;

    public AdapterPagesCascade(Context context, ArrayList<String> dataSet) {
        this.context = context;
        this.imagesResource = dataSet;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
    }

    public AdapterPagesCascade(Context context, ArrayList<String> dataSet, Fragment fragment) {
        this.context = context;
        this.imagesResource = dataSet;
        this.fragment = fragment;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        gestureDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e){
                ReaderMangaFragment f = (ReaderMangaFragment) fragment;
                f.controllShowBottomTopBar();
                return true;
            }
        });
        logoMangaStorageTemp = new LogoMangaStorageTemp(context);
        logoMangaStorage = new LogoMangaStorage(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PageCascadeItemBinding binding = PageCascadeItemBinding.inflate(LayoutInflater.from(context), parent, false);
        StartReadItemBinding startReadItemBinding = StartReadItemBinding.inflate(LayoutInflater.from(context),parent,false);
        EndReadItemBinding endReadItemBinding = EndReadItemBinding.inflate(LayoutInflater.from(context),parent,false);

        return viewType == VIEW_TYPE_ITEM?new ViewHolderItem(binding):viewType == VIEW_TYPE_HEADER?new StartReadViewHolder(startReadItemBinding):new AdapterPagesCascade.EndReadViewHolder(endReadItemBinding);
    }
    @Override
    public int getItemViewType(int position){

        return position == 0?VIEW_TYPE_HEADER:position == imagesResource.size()-1?VIEW_TYPE_FOOTER:VIEW_TYPE_ITEM;
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        holder.position = position;

        if(position >= this.imagesResource.size())return;

        if(position == 0){
            ((StartReadViewHolder)(holder)).binding.getRoot().setOnClickListener((v)->{((ReaderMangaFragment)fragment).controllShowBottomTopBar();});
            ((StartReadViewHolder)(holder)).position = holder.getAdapterPosition();
            if (this.isFirstChapter) {
                ((StartReadViewHolder)(holder)).binding.actionPrev.setVisibility(View.GONE);
                ((StartReadViewHolder)(holder)).binding.previousChapter.setVisibility(View.GONE);
            }else{
                ((StartReadViewHolder)(holder)).binding.previousChapter.setText(String.format("Capítulo anterior: %s - %s", previousCurrentAndNextChapter[0].getChapter(), previousCurrentAndNextChapter[0].title));
            }
            ((StartReadViewHolder)(holder)).binding.currentChapter.setText(String.format("Capítulo atual: %s - %s", previousCurrentAndNextChapter[1].getChapter(), previousCurrentAndNextChapter[1].title));


            ((StartReadViewHolder)(holder)).binding.actionPrev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ReaderMangaFragment f = (ReaderMangaFragment) fragment;
                    f.previousChapter();
                }
            });

            ((StartReadViewHolder)(holder)).binding.startRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ReaderMangaFragment f = (ReaderMangaFragment) fragment;
                    f.controllShowBottomTopBar();
                }
            });
            return;
        }
        if(holder.getAdapterPosition() == imagesResource.size()-1){
            ((EndReadViewHolder)(holder)).binding.getRoot().setOnClickListener((v)->{((ReaderMangaFragment)fragment).controllShowBottomTopBar();});
            ((EndReadViewHolder)(holder)).binding.timeWaste.setText(timeWasteString);
            ((EndReadViewHolder)(holder)).binding.nextChapterContainer.setVisibility(View.VISIBLE);
            ((EndReadViewHolder)(holder)).binding.actionEnd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ReaderMangaFragment f = (ReaderMangaFragment) fragment;
                    f.nextChapter();
                }
            });
            if (this.isLastChapter) {
                ((EndReadViewHolder)(holder)).binding.actionEnd.setVisibility(View.GONE);
                ((EndReadViewHolder)(holder)).binding.lastChap.setVisibility(View.VISIBLE);
                ((EndReadViewHolder)(holder)).position = holder.getAdapterPosition();
                ((EndReadViewHolder)(holder)).binding.nextChapter.setVisibility(View.GONE);
            }
            else{
                ((EndReadViewHolder)(holder)).binding.nextChapter.setText(String.format("Próximo capítulo: %s - %s", previousCurrentAndNextChapter[2].getChapter(), previousCurrentAndNextChapter[2].title));
            }
            ((EndReadViewHolder)(holder)).binding.currentChapterEnd.setText(String.format("Capítulo atual: %s - %s", previousCurrentAndNextChapter[1].getChapter(), previousCurrentAndNextChapter[1].title));

            ((EndReadViewHolder)(holder)).binding.nextChapterContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ReaderMangaFragment f = (ReaderMangaFragment) fragment;
                    f.controllShowBottomTopBar();
                }
            });
            return;
        }

        if(imagesResource.get(holder.getAdapterPosition()) == null)return;
//        System.out.println(position);
        ((ViewHolderItem)(holder)).position = holder.getAdapterPosition();
        SubsamplingScaleImageView img = ((ViewHolderItem)(holder)).binding.img;
        img.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        Glide.with(fragment.getActivity().getApplicationContext())
                .asBitmap()
                .load(imagesResource.get(holder.getAdapterPosition()))
                .override((int)screenWidth, (int)screenHeight)
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .downsample(DownsampleStrategy.AT_LEAST)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        float zoomScale = 1f;
                        img.setImage(ImageSource.bitmap(resource));
                        img.setDoubleTapZoomScale(2f);
                        img.setMinScale(1f);
                        ((ViewHolderItem)(holder)).binding.progressTop.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }
    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder){
        ReaderMangaFragment f = (ReaderMangaFragment) fragment;
        if(holder instanceof AdapterPagesCascade.ViewHolderItem){
            f.setPageCascade(((ViewHolderItem)holder).position);
            return;
        }
        if(holder instanceof AdapterPagesCascade.StartReadViewHolder) {
            f.setPageCascade(((StartReadViewHolder)holder).position);
            return;
        }
        f.setPageCascade(((EndReadViewHolder)holder).position);
    }
    @Override
    public int getItemCount() {
        return this.imagesResource.size();
    }
    @UiThread
    public void ignorePage(int toRemove){
        this.notifyItemRemoved(toRemove);
    }
    public int getAmountPagesIgnored(){
        return this.numPagesIgnored;
    }
    public static class ViewHolderItem extends RecyclerView.ViewHolder {
        public PageCascadeItemBinding binding;

        public EndReadItemBinding endReadItemBinding;
        private float zoomScale = 1f;
        private boolean isZoomed = false;
        public int position;
        public ViewHolderItem(@NonNull PageCascadeItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }


    }
    public static class StartReadViewHolder extends RecyclerView.ViewHolder{
        public StartReadItemBinding binding;
        public int position;
        public StartReadViewHolder(@NonNull StartReadItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
    public static class EndReadViewHolder extends RecyclerView.ViewHolder{
        public EndReadItemBinding binding;
        public int position;
        public EndReadViewHolder(@NonNull EndReadItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void setTimeWasteOnRead(long time) {


//        System.out.println(this.timeWaste);
        long auxTime = time;
        long hour = auxTime / 3600;
        auxTime = hour != 0 ? auxTime % 3600 : auxTime;
        long min = auxTime / 60;
        auxTime = min != 0 ? auxTime % 60 : auxTime;
        long sec = auxTime;

        if (hour != 0) {
            timeWasteString = "Tempo gasto: " + (hour > 9 ? "" : "0") + hour + (hour > 1 ? " horas " : " hora ") + ":" + (min > 9 ? "" : "0") + min + (min > 1 ? " minutos " : " minuto ") + ":" + (sec > 9 ? "" : "0") + sec + (sec > 1 ? " segundos " : " segundo ");
        } else if (min != 0) {
            timeWasteString = "Tempo gasto: " + (min > 9 ? "" : "0") + min + (min > 1 ? " minutos " : " minuto ") + ":" + (sec > 9 ? "" : "0") + sec + (sec > 1 ? " segundos " : " segundo ");
        } else {
            timeWasteString = "Tempo gasto: " + sec + (sec > 1 ? " segundos " : " segundo ");
        }

    }

    public boolean isLastChapter() {
        return isLastChapter;
    }

    public void setLastChapter(boolean lastChapter) {
        isLastChapter = lastChapter;
    }

    public boolean isFirstChapter() {
        return isFirstChapter;
    }

    public void setFirstChapter(boolean firstChapter) {
        isFirstChapter = firstChapter;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public String getLogoManga() {
        return LogoManga;
    }

    public void setLogoManga(String logoManga) {
        LogoManga = logoManga;
    }
}
