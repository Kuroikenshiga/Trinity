package com.example.trinity.storageAcess;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.trinity.Interfeces.Extensions;
import com.example.trinity.models.Model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;


public final class ChapterStorageManager {

    public final static String FOLDER_STORAGE = "chapterStorage";
    private Context context;
    private static String absolutePath;

    public ChapterStorageManager(Context context) {
        this.context = context;
    }

    public void createIfNotExistsFolderForChapters() {
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

    public File createIfNotExistFolderMangaChapters(String folderMangaName) {
        File folderMangaChapter = null;
        try {
            folderMangaChapter = new File(absolutePath, folderMangaName);
            if (!folderMangaChapter.exists()) {
                folderMangaChapter.mkdir();
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
        return folderMangaChapter;
    }

    public String saveChapterPage(Bitmap image, String fileName, File parent,String folder) {
        File imageChapter;
        if(parent == null){
            return "";
        }
        try {

            if (!parent.exists()) {
                return "";
            }

            imageChapter = new File(parent, fileName);

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

    public void getChapterPages(String idChapterApi, Handler h){
        Model model = Model.getInstance(context);
        int numPages = model.getNumberPagesDownloaded(idChapterApi);

        Message msg1 = Message.obtain();
        msg1.what = Extensions.RESPONSE_ITEM;
        Bundle bundle1 = new Bundle();
        bundle1.putInt("numPages",numPages);
        msg1.setData(bundle1);
        h.sendMessage(msg1);

        ContentValues[] values = model.getPagesDownloaded(idChapterApi);
        Bitmap bitmap = null;
        if(values == null)return;
        for(ContentValues v:values){
//            File bitmapFile = new File(v.getAsString("path"));
//            if(!bitmapFile.exists()){
////                System.out.println("Arquivo inexistente");
//                bitmap = null;
//            }
//            try(FileInputStream fileInputStream = new FileInputStream(bitmapFile)){
//                bitmap = BitmapFactory.decodeStream(fileInputStream);
//            }catch (IOException ex){
//                bitmap = null;
//                ex.printStackTrace();
//            }
            Message msg = Message.obtain();
            msg.what = Extensions.RESPONSE_PAGE;
            Bundle bundle = new Bundle();
            bundle.putString("img",v.getAsString("path"));
            bundle.putInt("index",v.getAsInteger("page"));
            msg.setData(bundle);
            h.sendMessage(msg);
        }
    }
    public boolean clearStorage(){
        try {
            File file = new File(this.context.getFilesDir(), FOLDER_STORAGE);
            if (!file.exists()) {
                return false;
            }

            for(File child: Objects.requireNonNull(file.listFiles())){
                for(File childChild: Objects.requireNonNull(child.listFiles())){
                    if(!childChild.delete()){
                        return false;
                    }
                }
                if(!child.delete()){
                    return false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


        return true;
    }
}
