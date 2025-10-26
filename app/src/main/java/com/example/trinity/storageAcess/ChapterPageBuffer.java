package com.example.trinity.storageAcess;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.WorkerThread;

import com.example.trinity.Interfaces.PageStorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Objects;

public class ChapterPageBuffer implements PageStorage {

    public final static String FOLDER_STORAGE = "chapterBufferStorage";
    private Context context;
    private String absolutePath;
    private static ChapterPageBuffer instance = null;

    public static ChapterPageBuffer getInstance(Context context){
        if(instance == null) {
            instance = new ChapterPageBuffer(context);
            instance.createIfNotExistPageFolder();
//            absolutePath = new File(context.getFilesDir(), FOLDER_STORAGE).getAbsolutePath();
            return instance;
        }
//        absolutePath = new File(context.getFilesDir(), FOLDER_STORAGE).getAbsolutePath();
        instance.createIfNotExistPageFolder();
        return instance;

    }
    private ChapterPageBuffer(Context context) {
        this.context = context;
    }
    @WorkerThread
    public void createIfNotExistPageFolder(){
        try {
            File file = new File(this.context.getFilesDir(), FOLDER_STORAGE);
            if (!file.exists()) {
                file.mkdir();
            }
            absolutePath = file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @WorkerThread
    public String insertBitmapInFolder(Bitmap image, String fileName) {
        File imageChapter;

        try {
            fileName = fileName.split("[.]")[0].concat("-"+Long.toString(Instant.now().toEpochMilli()))+".jpeg";
            imageChapter = new File(absolutePath, fileName);

            try (FileOutputStream outputStream = new FileOutputStream(imageChapter)) {

                image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
                return "";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
        return imageChapter.getAbsolutePath();
    }
    public File[] getAllPagesInBuffer(){
        File file = new File(absolutePath);
        if (!file.exists()) {
            return new File[]{};
        }
        return Objects.requireNonNull(file.listFiles());
    }
    @WorkerThread
    public void clearFolder(){
        File file = new File(absolutePath);
        if (!file.exists()) {
            return;
        }
        for (File child : Objects.requireNonNull(file.listFiles())) {
            child.delete();
        }
    }
}
