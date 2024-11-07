package com.example.trinity.storageAcess;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Objects;

//import okhttp3.internal.cache.DiskLruCache;
import com.bumptech.glide.disklrucache.DiskLruCache;

public final class PageCacheManager {

    public final String CACHE_DIR = "pageCache";
    public final int DISK_CACHE_SIZE = 1024*1024*300;
    private boolean isEmpty = true;
    private File dirCache;
    private Context context;
    private static String absolutePath;
    private static PageCacheManager instance;
    public static PageCacheManager getInstance(Context context){
        if(instance == null){
            instance = new PageCacheManager(context);
            return instance;
        }
        return instance;
    }

    private PageCacheManager(Context context) {
        this.context = context;
    }

    public void createIfNotExistCacheChapterFolder(){
        File dir = new File(context.getFilesDir(),CACHE_DIR);
        if(!dir.exists()){
            dir.mkdir();
        }
        absolutePath = dir.getAbsolutePath();
    }

    public String insertBitmapInCache(Bitmap bitmap,String key) {
        dirCache = new File(absolutePath);
        File image = null;
        if(!dirCache.exists())return "";

        image = new File(dirCache,key);
        if(image.exists())return "";

        try(OutputStream outputStream = Files.newOutputStream(image.toPath())){
            if(bitmap != null){
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
            }
            outputStream.flush();
        }catch (IOException ex){
            ex.printStackTrace();
            return "";
        }
        return image.getAbsolutePath();
    }
//    public Bitmap getBitmapFromCache(String key){
//        Bitmap bitmap = null;
//        dirCache = new File(absolutePath);
//        if(!dirCache.exists())return null;
//
//        File image = new File(dirCache,key);
//        if(!image.exists())return null;
//
//        try(FileInputStream inputStream = new FileInputStream(image)){
//            bitmap = BitmapFactory.decodeStream(inputStream);
//        }catch (IOException ex){
//            ex.printStackTrace();
//            return null;
//        }
//        return bitmap;
//    }
    public String getBitmapFromCache(String key){
        dirCache = new File(absolutePath);
        if(!dirCache.exists())return "";

        File image = new File(dirCache,key);
        if(!image.exists())return "";
        return image.getAbsolutePath();
    }
    public void clearCache(){
        dirCache = new File(absolutePath);
        for(File file: Objects.requireNonNull(dirCache.listFiles())){
            file.delete();
        }
    }

}
