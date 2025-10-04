package com.example.trinity.services;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.trinity.Interfaces.PageStorage;
import com.example.trinity.storageAcess.PageCacheManager;

public class ClearPageCacheWork extends Worker {
    PageStorage pageCacheManager;
    public ClearPageCacheWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        pageCacheManager = PageCacheManager.getInstance(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        pageCacheManager.clearFolder();
        return Result.success();
    }
}
