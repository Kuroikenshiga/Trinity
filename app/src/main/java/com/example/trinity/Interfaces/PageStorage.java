package com.example.trinity.Interfaces;

import android.content.Context;
import android.graphics.Bitmap;

import com.example.trinity.storageAcess.PageCacheManager;

public interface PageStorage {

    static PageStorage getInstance(Context context) {
        return null;
    }

    void createIfNotExistPageFolder();
    String insertBitmapInFolder(Bitmap bitmap, String key);
    void clearFolder();
}
