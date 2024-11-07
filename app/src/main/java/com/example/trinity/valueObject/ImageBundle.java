package com.example.trinity.valueObject;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.ArrayList;

public class ImageBundle {

    private ArrayList<Bitmap> resource;
    private int position;
    private Context context;
    private Bitmap originalImage;
    public ImageBundle(Bitmap originalImage, int position,Context c) {
        this.originalImage = originalImage;
        this.position = position;
        this.context = c;
        resource = new ArrayList<>();
        loadResource();
    }
    private void loadResource(){
        if(originalImage == null || context == null)return;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        double scaleRate = (double) screenWidth /originalImage.getWidth();
        float width = originalImage.getWidth();

        if(originalImage.getHeight()*scaleRate > screenHeight){

            int imageHeight = originalImage.getHeight();
            screenHeight = scaleRate > 1?(int)(screenHeight/scaleRate):screenHeight;
            int sliceHeight = screenHeight,offset = 0;
            while(offset < originalImage.getHeight()){

                sliceHeight = screenHeight+offset > originalImage.getHeight()?(originalImage.getHeight()-offset):sliceHeight;
//                System.out.println(sliceHeight+offset);
                Bitmap cropedImage = Bitmap.createBitmap(originalImage,0,offset,originalImage.getWidth(),sliceHeight);

                offset+=sliceHeight;

                this.resource.add(cropedImage);

            }
            return;
        }
        this.resource.add(originalImage);
    }

    public ArrayList<Bitmap> getResource() {
        return resource;
    }
}
