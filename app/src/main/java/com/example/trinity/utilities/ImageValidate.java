package com.example.trinity.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import androidx.annotation.NonNull;

public abstract class ImageValidate {

    public static boolean isSubImage(@NonNull Bitmap bitmap) {
        return bitmap.getHeight() == 100;
    }

    public static Bitmap BitmapConcat(Bitmap originalImage, Bitmap subImage) {

        Bitmap bitmap = Bitmap.createBitmap(originalImage.getWidth(), originalImage.getHeight()+subImage.getHeight(),originalImage.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(originalImage, 0, 0, null);
        canvas.drawBitmap(subImage, 0, originalImage.getHeight(), null);
        return bitmap;
    }
}
