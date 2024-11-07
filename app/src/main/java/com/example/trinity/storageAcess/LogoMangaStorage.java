package com.example.trinity.storageAcess;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LogoMangaStorage {
    private Context context;
    private static final String NAME_STORAGE = "logoMangaStorage";
    private static String absolutePath;

    public LogoMangaStorage(Context context) {
        this.context = context;
    }

    public void createIfNotExistsFolderForLogos(){
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
    public String insertLogoManga(Bitmap logo,String idApiManga){
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
    public String getLogoFromStorage(String idApiManga){
        idApiManga += ".jpeg";
        File dir = new File(absolutePath);
        if(!dir.exists())return "";
        try{
            File logo = new File(dir,idApiManga);
            if(!logo.exists())return "";
            return logo.getAbsolutePath();
        }catch (NullPointerException ex){
            ex.printStackTrace();
            return "";
        }
    }
    public boolean receiveFile(String path) {
        LogoMangaStorageTemp storageTemp = new LogoMangaStorageTemp(context);
        File file = new File(storageTemp.getLogoFromTempStorage(path));
        path += ".jpeg";
        File dest = new File(absolutePath,path);
        if(!file.exists())return false;
        if(dest.exists())return true;

        try(FileInputStream fileInputStream = new FileInputStream(file)){
            Bitmap logo = BitmapFactory.decodeStream(fileInputStream);
            try(FileOutputStream outputStream = new FileOutputStream(dest)){
                if(logo != null){
                    logo.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                }
                outputStream.flush();
            }
        }catch (IOException ex){
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    public boolean removeLogo(String path){
        LogoMangaStorageTemp logoMangaStorageTemp = new LogoMangaStorageTemp(context);
        if(!logoMangaStorageTemp.getLogoFromTempStorage(path).isEmpty())return true;
        path += ".jpeg";
        File file = new File(absolutePath,path);
        if(!file.exists())return false;
        try(FileInputStream inputStream = new FileInputStream(file)){
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return !logoMangaStorageTemp.insertLogoManga(bitmap,path.split("[.]")[0]).isEmpty();
        }catch (IOException ex){
            ex.printStackTrace();
            return false;
        }


    }
}
