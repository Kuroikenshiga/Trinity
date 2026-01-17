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

    private static final float minGrayPixelsPercentage = 0.4f;

    public static boolean isSubImage(@NonNull Bitmap originalImage, Bitmap subImage,float colorScale) {
//        System.out.println("altura° - "+(originalImage.getHeight()+subImage.getHeight()));
//        System.out.println("largura° - "+originalImage.getWidth());

        if(originalImage.getWidth() != subImage.getWidth())return false;

        float imageRatio = (float) originalImage.getWidth() /(subImage.getHeight()+originalImage.getHeight());
        boolean initialTest;
        if(originalImage.getWidth() < originalImage.getHeight() + subImage.getHeight()){
            initialTest = (imageRatio > minVerticalRatioForCompute && imageRatio < maxVerticalRatioForCompute);
        }else{
            initialTest = (imageRatio > minHorizontalRatioForCompute && imageRatio < maxHorizontalRatioForCompute);
        }

        if(!initialTest)return false;
        return verifyColorScale(originalImage,subImage,colorScale);

    }

    public static Bitmap BitmapConcat(Bitmap originalImage, Bitmap subImage) {

        Bitmap bitmap = Bitmap.createBitmap(originalImage.getWidth(), originalImage.getHeight()+subImage.getHeight(),originalImage.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(originalImage, 0, 0, null);
        canvas.drawBitmap(subImage, 0, originalImage.getHeight(), null);
        return bitmap;
    }
    private static boolean verifyColorScale(@NonNull Bitmap originalImage, Bitmap subImage,float colorScale){
        int totalPixelsOriginalImage = 0,totalPixelsSubImage = 0,totalGrayPixelsOriginalImage = 0,totalGrayPixelsSubImage = 0;
        boolean isOriginalImageGrayScale = true;
        List<Palette.Swatch>list = Palette.from(originalImage).generate().getSwatches();
        for(Palette.Swatch swatch:list) {
            if(swatch.getHsl()[1] <= 0.1f){
                isOriginalImageGrayScale = false;
                totalGrayPixelsOriginalImage += swatch.getPopulation();
            }
            totalPixelsOriginalImage += swatch.getPopulation();
        }
        isOriginalImageGrayScale = (float)totalGrayPixelsOriginalImage / totalPixelsOriginalImage >= colorScale;

//        System.out.println("1 - "+((float)totalGrayPixelsOriginalImage / totalPixelsOriginalImage));

        boolean isSubImageGrayScale = true;
        list = Palette.from(subImage).generate().getSwatches();
        for(Palette.Swatch swatch:list) {
            if(swatch.getHsl()[1] <= 0.1f){
                totalGrayPixelsSubImage += swatch.getPopulation();
            }
            totalPixelsSubImage += swatch.getPopulation();
        }
        isSubImageGrayScale = (float)totalGrayPixelsSubImage / totalPixelsSubImage >= colorScale;
//        System.out.println("2 - "+((float)totalGrayPixelsSubImage / totalPixelsSubImage));


        return isOriginalImageGrayScale == isSubImageGrayScale;
    }
}
