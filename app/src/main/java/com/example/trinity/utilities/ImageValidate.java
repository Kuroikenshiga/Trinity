package com.example.trinity.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import androidx.annotation.NonNull;

public abstract class ImageValidate {

    private static final float minVerticalRatioForCompute = 0.6f;
    private static final float maxVerticalRatioForCompute = 1f;
    private static final float minHorizontalRatioForCompute = 1.2f;
    private static final float maxHorizontalRatioForCompute = 1.6f;

    public static boolean isSubImage(@NonNull Bitmap originalImage, Bitmap subImage) {
        float imageRatio = (float) originalImage.getWidth() /(subImage.getHeight()+originalImage.getHeight());
        return originalImage.getWidth() < originalImage.getHeight() + subImage.getHeight()?(imageRatio > minVerticalRatioForCompute && imageRatio < maxVerticalRatioForCompute):(imageRatio > minHorizontalRatioForCompute && imageRatio < maxHorizontalRatioForCompute);

    }

    public static Bitmap BitmapConcat(Bitmap originalImage, Bitmap subImage) {

        Bitmap bitmap = Bitmap.createBitmap(originalImage.getWidth(), originalImage.getHeight()+subImage.getHeight(),originalImage.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(originalImage, 0, 0, null);
        canvas.drawBitmap(subImage, 0, originalImage.getHeight(), null);
        return bitmap;
    }
}
