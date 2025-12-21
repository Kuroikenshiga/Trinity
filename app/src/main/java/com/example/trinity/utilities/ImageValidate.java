package com.example.trinity.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import androidx.palette.graphics.Palette;
import androidx.annotation.NonNull;

import java.util.List;

public abstract class ImageValidate {

    private static final float minVerticalRatioForCompute = 0.6f;
    private static final float maxVerticalRatioForCompute = 1f;

    private static final float minHorizontalRatioForCompute = 1.2f;
    private static final float maxHorizontalRatioForCompute = 1.6f;

    public static boolean isSubImage(@NonNull Bitmap originalImage, Bitmap subImage) {
        float imageRatio = (float) originalImage.getWidth() /(subImage.getHeight()+originalImage.getHeight());
        boolean initialTest = originalImage.getWidth() < originalImage.getHeight() + subImage.getHeight()?(imageRatio > minVerticalRatioForCompute && imageRatio < maxVerticalRatioForCompute):(imageRatio > minHorizontalRatioForCompute && imageRatio < maxHorizontalRatioForCompute);
        if(!initialTest)return false;
        return verifyColorScale(originalImage,subImage);

    }

    public static Bitmap BitmapConcat(Bitmap originalImage, Bitmap subImage) {

        Bitmap bitmap = Bitmap.createBitmap(originalImage.getWidth(), originalImage.getHeight()+subImage.getHeight(),originalImage.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(originalImage, 0, 0, null);
        canvas.drawBitmap(subImage, 0, originalImage.getHeight(), null);
        return bitmap;
    }
    private static boolean verifyColorScale(@NonNull Bitmap originalImage, Bitmap subImage){
        boolean isOriginalImageGrayScale = true;
        List<Palette.Swatch>list = Palette.from(originalImage).generate().getSwatches();
        for(Palette.Swatch swatch:list) {
            if(swatch.getHsl()[1] > 0.1f){
                isOriginalImageGrayScale = false;
                break;
            }
        }
        boolean isSubImageGrayScale = true;
        list = Palette.from(subImage).generate().getSwatches();
        for(Palette.Swatch swatch:list) {
            if(swatch.getHsl()[1] > 0.1f){
                isSubImageGrayScale = false;
                break;
            }
        }
        return isOriginalImageGrayScale == isSubImageGrayScale;
    }
}
