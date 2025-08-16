package com.example.trinity.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;


import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.example.trinity.MangaShowContentActivity;
import com.example.trinity.R;
import com.example.trinity.databinding.PageItemBinding;
import com.example.trinity.fragments.ReaderMangaFragment;
import com.example.trinity.storageAcess.LogoMangaStorage;
import com.example.trinity.storageAcess.LogoMangaStorageTemp;
import com.example.trinity.storageAcess.PageCacheManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class AdapterPages extends RecyclerView.Adapter<AdapterPages.ImageViewHolder> {
    private Context context;
    private ArrayList<String> resourcesImage;
    private String timeWasteString = "";
    private boolean isLastChapter = false;
    private boolean isFirstChapter = false;
    private String LogoManga;
    private float alpha;
    private boolean isReverseStartReadLogo = false;
    private Fragment fragment;
    private double screenWidth, screenHeight;
    private double screenRatio;
    public int currentItem = 0;
    private boolean isFavorited = false;
    private LogoMangaStorage logoMangaStorage;
    private LogoMangaStorageTemp logoMangaStorageTemp;
    private int numPagesIgnored = 0;
    public AdapterPages(Context c, @NonNull ArrayList<String> array) {
        this.context = c;
        this.resourcesImage = array;
        alpha = 1f;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenRatio = (double) (screenHeight / screenWidth);
        logoMangaStorageTemp = new LogoMangaStorageTemp(c);
        logoMangaStorage = new LogoMangaStorage(c);
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PageItemBinding b = PageItemBinding.inflate(LayoutInflater.from(this.context), parent, false);
        ImageViewHolder holder = new ImageViewHolder(b);
        holder.screenWidth = (float)screenWidth;
        holder.screenHeight = (float)this.screenHeight;
        return holder ;

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        
        
        
        if (holder.getAdapterPosition() == this.resourcesImage.size() - 1 - numPagesIgnored) {

            holder.binding.timeWaste.setText(timeWasteString);
            holder.binding.imgContainer.setVisibility(View.GONE);
            holder.binding.startRead.setVisibility(View.GONE);
            holder.binding.nextChapterContainer.setVisibility(View.VISIBLE);
            holder.binding.actionEnd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ReaderMangaFragment f = (ReaderMangaFragment) fragment;
                    f.nextChapter();
                }
            });
            if (this.isLastChapter) {
                holder.binding.endLogo.setImageResource(R.drawable.end_chapters);
                holder.binding.actionEnd.setVisibility(View.GONE);
                holder.binding.lastChap.setVisibility(View.VISIBLE);

            }
            holder.binding.nextChapterContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ReaderMangaFragment f = (ReaderMangaFragment) fragment;
                    f.controllShowBottomTopBar();
                }
            });
            return;
        }
        if (holder.getAdapterPosition() == 0) {


            holder.binding.imgContainer.setVisibility(View.GONE);
            holder.binding.nextChapterContainer.setVisibility(View.GONE);
            holder.binding.startRead.setVisibility(View.VISIBLE);
            if(fragment.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)){
                Glide.with(fragment)
                        .load(logoMangaStorage.getLogoFromStorage(LogoManga).isEmpty()?logoMangaStorageTemp.getLogoFromTempStorage(LogoManga):logoMangaStorage.getLogoFromStorage(LogoManga))
                        .into(holder.binding.backGroundManga);


            }

            if (isReverseStartReadLogo) {
                holder.binding.startReadLogo.setScaleX(-1);
                holder.binding.startReadLogo.setScaleY(-1);
            } else {
                holder.binding.startReadLogo.setScaleX(1);
                holder.binding.startReadLogo.setScaleY(1);
            }

            if (this.isFirstChapter) {
                holder.binding.actionPrev.setVisibility(View.GONE);

            }
            holder.binding.actionPrev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ReaderMangaFragment f = (ReaderMangaFragment) fragment;
                    f.previousChapter();
                }
            });
            holder.binding.startReadLogo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ReaderMangaFragment f = (ReaderMangaFragment) fragment;
                    f.nextPage();
                }
            });
            holder.binding.startRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ReaderMangaFragment f = (ReaderMangaFragment) fragment;
                    f.controllShowBottomTopBar();
                }
            });

            return;
        }

        holder.binding.startRead.setVisibility(View.GONE);
        holder.binding.nextChapterContainer.setVisibility(View.GONE);
        holder.binding.imgContainer.setVisibility(View.VISIBLE);

        if (resourcesImage.get(holder.getAdapterPosition()) != null) {

            holder.page = holder.getAdapterPosition();

            SubsamplingScaleImageView img = holder.binding.img;

            GestureDetector gestureDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
               @Override
               public boolean onSingleTapConfirmed(MotionEvent e){
                   ReaderMangaFragment f = (ReaderMangaFragment) fragment;
                    f.controllShowBottomTopBar();
                   return true;
               }
            });
            img.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });


            if(fragment.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {

                Glide.with(fragment.getActivity().getApplicationContext())
                        .asBitmap()
                        .load(resourcesImage.get(holder.getAdapterPosition()))
                        .override(context.getResources().getDisplayMetrics().widthPixels, context.getResources().getDisplayMetrics().heightPixels)
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
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
                holder.binding.imgContainer.setVisibility(View.VISIBLE);
                holder.binding.getRoot().bringChildToFront(holder.binding.progressTop);
                holder.binding.progressTop.setVisibility(View.GONE);

                ReaderMangaFragment f = (ReaderMangaFragment) fragment;
                f.controllUserInput(true);
            }
        }

    }
    
    @Override
    public int getItemCount() {
        return this.resourcesImage.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        private PageItemBinding binding;
        final float minScale = 1f;
        final float maxScale = 3f;
        public int page = 0;
        public boolean isAlredyInFullResolution = false;
        public CustomTarget<Bitmap> bitmapCustomTarget;
        public float screenWidth,screenHeight;
        public ImageViewHolder(PageItemBinding binding) {
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

    public void setIslastChapter(boolean isLastChapter) {
        this.isLastChapter = isLastChapter;
    }

    public void setIsFirstChapter(boolean isFirstChapter) {
        this.isFirstChapter = isFirstChapter;
    }

    public void setLogoManga(String logoManga) {
        LogoManga = logoManga;

    }

    public void setReverseStartReadLogo(boolean reverseStartReadLogo) {
        isReverseStartReadLogo = reverseStartReadLogo;
    }

    public void setAlpha(float alpha) {

        this.alpha = alpha / 100;
//        System.out.println(this.alpha);
    }

    public void setFragment(Fragment f) {
        this.fragment = f;
    }

    @UiThread
    public void ignorePage(){
        int toRemove = this.resourcesImage.size() - 1;
        this.resourcesImage.remove(toRemove);
        this.notifyItemRemoved(toRemove);
    }
    @Deprecated
    public int getAmountPagesIgnored(){
        return this.numPagesIgnored;
    }
//    public void setFavorited(boolean favorited) {
//        isFavorited = favorited;
////        System.out.println(isFavorited);
//        if(favorited){
//            logoMangaStorage = new LogoMangaStorage(context);
//            logoMangaStorageTemp = null;
//            return;
//        }
//        logoMangaStorageTemp = new LogoMangaStorageTemp(context);
//        logoMangaStorage = null;
//    }
}
