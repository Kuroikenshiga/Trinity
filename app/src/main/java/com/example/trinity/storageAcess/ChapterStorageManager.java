package com.example.trinity.storageAcess;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.widget.Toast;

import com.example.trinity.Interfaces.Extensions;
import com.example.trinity.models.Model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    public String saveChapterPage(File img, File parent) {
        String path = "";
        if (parent == null) {
            return "";
        }
        try {

            if (!parent.exists()) {
                return "";
            }
            File targetMove = new File(parent,img.getName());
            try {

                path = Files.move(Paths.get(img.getAbsolutePath()), Paths.get(targetMove.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING).toString();

            } catch (IOException ex) {
                ex.printStackTrace();
                return "";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
        return path;
    }

    public void getChapterPages(String idChapterApi, Handler h) {
        Model model = Model.getInstance(context);
        int numPages = model.getNumberPagesDownloaded(idChapterApi);

        Message msg1 = Message.obtain();
        msg1.what = Extensions.RESPONSE_ITEM;
        Bundle bundle1 = new Bundle();
        bundle1.putInt("numPages", numPages);
        msg1.setData(bundle1);
        h.sendMessage(msg1);

        ContentValues[] values = model.getPagesDownloaded(idChapterApi);
        Bitmap bitmap = null;
        if (values == null) return;
        for (ContentValues v : values) {
            Message msg = Message.obtain();
            msg.what = Extensions.RESPONSE_PAGE;
            Bundle bundle = new Bundle();
            bundle.putString("img", v.getAsString("path"));
            bundle.putInt("index", v.getAsInteger("page"));
            msg.setData(bundle);
            h.sendMessage(msg);
        }
    }

    public boolean clearStorage() {
        try {
            File file = new File(this.context.getFilesDir(), FOLDER_STORAGE);
            if (!file.exists()) {
                return false;
            }

            for (File child : Objects.requireNonNull(file.listFiles())) {
                for (File childChild : Objects.requireNonNull(child.listFiles())) {
                    if (!childChild.delete()) {
                        return false;
                    }
                }
                if (!child.delete()) {
                    return false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


        return true;
    }

    public boolean pageDownload(String imageName,String url){
        if (url == null) {
            return false;
        }
        Bitmap image = null;
        try (FileInputStream fileInputStream = new FileInputStream(new File(url))) {

            image = BitmapFactory.decodeStream(fileInputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }

        if (image == null) {
            return false;
        }

        ContentValues imageValue = new ContentValues();
        imageValue.put(MediaStore.MediaColumns.DISPLAY_NAME, imageName);
        imageValue.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        imageValue.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        ContentResolver contentResolver = context.getContentResolver();

        Uri uri = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, imageValue);
        }

        if (uri == null) return false;

        try (OutputStream outputStream = contentResolver.openOutputStream(uri)) {

            if (outputStream == null) return false;

            image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    return true;
    }

}
