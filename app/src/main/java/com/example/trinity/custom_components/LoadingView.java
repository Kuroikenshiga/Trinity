package com.example.trinity.custom_components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.trinity.R;
import com.example.trinity.databinding.LoadigViewBinding;

public class LoadingView extends LinearLayout {

    private LoadigViewBinding binding;
    private LayoutParams layoutParamsLeft;
    private LayoutParams layoutParamsRight;
    private ValueAnimator animatorLeft;
    private ValueAnimator animatorRight;

    public LoadingView(Context context) {
        super(context);
        this.init(context,null);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.init(context,attrs);
    }

    private void init(Context context,AttributeSet attributeSet){
        inflate(context,R.layout.loadig_view,this);
        if(attributeSet != null){
            try(TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.LoadingView)){
                int colorPrimary = typedArray.getResourceId(R.styleable.LoadingView_primaryColor,0);
                int colorSecondary = typedArray.getResourceId(R.styleable.LoadingView_secondaryColor,0);

                if(colorPrimary != 0){
                    findViewById(R.id.left).setBackgroundColor(context.getColor(colorPrimary));
                }
                if(colorSecondary != 0){
                    findViewById(R.id.right).setBackgroundColor(context.getColor(colorSecondary));
                }
            }
        }
        animateLoading(context);
    }

    private void animateLoading(Context context){
        layoutParamsLeft = (LinearLayout.LayoutParams) findViewById(R.id.left).getLayoutParams();
        layoutParamsRight = (LinearLayout.LayoutParams) findViewById(R.id.right).getLayoutParams();

        animatorLeft = ValueAnimator.ofInt(30, 10);
        animatorRight = ValueAnimator.ofInt(10, 30);
        animatorLeft.setRepeatCount(ValueAnimator.INFINITE);
        animatorRight.setRepeatCount(ValueAnimator.INFINITE);
        animatorLeft.setRepeatMode(ValueAnimator.REVERSE);
        animatorRight.setRepeatMode(ValueAnimator.REVERSE);
        animatorLeft.setDuration(300);
        animatorRight.setDuration(300);

        animatorLeft.addUpdateListener(animation -> {
            layoutParamsLeft.width = (int) animation.getAnimatedValue() * (int) context.getResources().getDisplayMetrics().density;
            layoutParamsLeft.height = (int) animation.getAnimatedValue() * (int) context.getResources().getDisplayMetrics().density;
            findViewById(R.id.left).setLayoutParams(layoutParamsLeft);
        });

        animatorRight.addUpdateListener(animation -> {
            layoutParamsRight.width = (int) animation.getAnimatedValue() * (int) context.getResources().getDisplayMetrics().density;
            layoutParamsRight.height = (int) animation.getAnimatedValue() * (int) context.getResources().getDisplayMetrics().density;
            findViewById(R.id.right).setLayoutParams(layoutParamsRight);
        });

        if(this.getVisibility() != GONE && this.getVisibility() != INVISIBLE){
            animatorLeft.start();
            animatorRight.start();
        }
    }
    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE || visibility == INVISIBLE){
            if(this.animatorLeft.isRunning())this.animatorLeft.cancel();
            if(this.animatorRight.isRunning())this.animatorRight.cancel();
        }
        else{
            this.animatorLeft.start();
            this.animatorRight.start();
        }
    }



}
