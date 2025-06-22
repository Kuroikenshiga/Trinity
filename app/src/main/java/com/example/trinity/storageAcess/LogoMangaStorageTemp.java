package com.example.trinity.storageAcess;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

public class LogoMangaStorageTemp {


    private Context context;
    private static final String NAME_STORAGE = "logoMangaStorageTemp";
    private static String absolutePath;

    public LogoMangaStorageTemp(Context context) {
        this.context = context;
    }

    public void createIfNotExistsFolderTempForLogos(){
        File file = null;
        try{
            file = new File(context.getFilesDir(),NAME_STORAGE);
            if(!file.exists()){
                file.mkdir();
            }
        }catch (NullPointerException ex){
            ex.printStackTrace();
            return;
        }
        absolutePath = file.getAbsolutePath();
    }
    public String insertLogoManga(Bitmap logo, String idApiManga){
        File dir = new File(absolutePath);

        idApiManga += ".jpeg";

        if(!dir.exists() || logo == null)return "";

        try{
            File logoImage = new File(dir,idApiManga);
            if(logoImage.exists()){
                return logoImage.getAbsolutePath();
            }
            try(FileOutputStream outputStream = new FileOutputStream(logoImage)){
                logo.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                outputStream.flush();
            }catch (Exception ex){
                ex.printStackTrace();
                return "";
            }
            return logoImage.getAbsolutePath();
        }catch (NullPointerException ex){
            ex.printStackTrace();
            return "";
        }
    }

    public String insertLogoManga(Bitmap logo, String idApiManga,String format){
        File dir = new File(absolutePath);

        idApiManga += "."+format;

        if(!dir.exists() || logo == null)return "";

        try{
            File logoImage = new File(dir,idApiManga);
            if(logoImage.exists()){
                return logoImage.getAbsolutePath();
            }
            try(FileOutputStream outputStream = new FileOutputStream(logoImage)){
                logo.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                outputStream.flush();
            }catch (Exception ex){
                ex.printStackTrace();
                return "";
            }
            return logoImage.getAbsolutePath();
        }catch (NullPointerException ex){
            ex.printStackTrace();
            return "";
        }
    }

    public String getLogoFromTempStorage(String idApiManga){
        idApiManga += ".jpeg";

        File dir = new File(absolutePath);
        if(!dir.exists())return "";
        try{
            File logo = new File(dir,idApiManga);
            if(!logo.exists()){
                logo = new File(dir,idApiManga.split("[/]")[idApiManga.split("[/]").length-1]);
                if(!logo.exists())return "";
            };
            return logo.getAbsolutePath();
        }catch (NullPointerException ex){
            ex.printStackTrace();
            return "";
        }
    }
    public void clearLogosFromStorage(){
        File file = new File(absolutePath);
        for(File child: Objects.requireNonNull(file.listFiles())){
            child.delete();
        }
    }

}
