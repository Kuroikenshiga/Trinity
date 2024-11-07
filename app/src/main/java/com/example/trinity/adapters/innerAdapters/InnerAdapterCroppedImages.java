package com.example.trinity.adapters.innerAdapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.trinity.databinding.CroppedPageCascadeItemBinding;
import com.example.trinity.fragments.ReaderMangaFragment;


import java.util.ArrayList;

public class InnerAdapterCroppedImages extends RecyclerView.Adapter<InnerAdapterCroppedImages.CroppedImageViewHolder> {

    private Context context;
    private ArrayList<Bitmap> imageResource;
    private Fragment fragment;


    public InnerAdapterCroppedImages(Context context, ArrayList<Bitmap> imageResource) {
        this.context = context;
        this.imageResource = imageResource;

    }

    @NonNull
    @Override
    public CroppedImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CroppedImageViewHolder(CroppedPageCascadeItemBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CroppedImageViewHolder holder, int position) {

        if (this.imageResource.get(position) != null) {
//            PhotoView img = holder.binding.croppedImage;
//            Glide.with(context)
//                    .load(this.imageResource.get(position))
//                    .override(context.getResources().getDisplayMetrics().widthPixels, context.getResources().getDisplayMetrics().heightPixels)
//                    .into(img);
//
//
//            img.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
//                @Override
//                public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
//                    ReaderMangaFragment f = (ReaderMangaFragment) fragment;
//                    f.controllShowBottomTopBar();
//                    return false;
//                }
//
//                @Override
//                public boolean onDoubleTap(@NonNull MotionEvent e) {
//                    if(img.getScale() == 1f){
//                        img.setScale(2f,e.getX(),e.getY(),true);
//                        return false;
//                    }
//                    img.setScale(1f,true);
//                    return false;
//                }
//
//                @Override
//                public boolean onDoubleTapEvent(@NonNull MotionEvent e) {
//                    return true;
//
//                }
//            });
        }


    }

    @Override
    public int getItemCount() {
        return this.imageResource.size();
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public static class CroppedImageViewHolder extends RecyclerView.ViewHolder {

        CroppedPageCascadeItemBinding binding;

        public CroppedImageViewHolder(@NonNull CroppedPageCascadeItemBinding b) {
            super(b.getRoot());
            this.binding = b;
        }

    }


}
