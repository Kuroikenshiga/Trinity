package com.example.trinity.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.graphics.BitmapCompat;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class ImageEditor {

    private Context context;

    public ImageEditor(Context context) {
        this.context = context;
    }

    static public Bitmap downgradeImage(Bitmap b) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


        b.compress(Bitmap.CompressFormat.JPEG, 0, outputStream);

//        b.recycle();
        return BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.size());
    }

    static public int getSampleImage(int bWidth,int bHeiht, int screenWidth, int screenHeight) {
        int sample = 1;
        float pivot = (float)bWidth >= bHeiht?bWidth:bHeiht;
        if(pivot <= 10000){
            return 1;
        }
        while(pivot > 10000){
            sample *= 2;
            pivot /= sample;
        }
        return sample;
    }
}
