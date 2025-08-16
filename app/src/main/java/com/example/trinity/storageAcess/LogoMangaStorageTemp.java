package com.example.trinity.storageAcess;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

    public boolean receiveFile(String path) {
        LogoMangaStorage storage = new LogoMangaStorage(context);
        File file = new File(storage.getLogoFromStorage(path));
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
