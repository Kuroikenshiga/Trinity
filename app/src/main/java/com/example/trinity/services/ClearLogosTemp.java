package com.example.trinity.services;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.trinity.storageAcess.LogoMangaStorageTemp;

public class ClearLogosTemp extends Worker {
    LogoMangaStorageTemp storageTemp;
    public ClearLogosTemp(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        storageTemp = new LogoMangaStorageTemp(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        storageTemp.clearLogosFromStorage();
        return Result.success();
    }
}
