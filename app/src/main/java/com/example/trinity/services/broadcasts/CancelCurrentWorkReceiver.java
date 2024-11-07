package com.example.trinity.services.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.WorkManager;

public class CancelCurrentWorkReceiver extends BroadcastReceiver {
    private static boolean isWorkUpdatesLibraryCanceled = false;
    private static boolean isWorkDownloadChaptersCanceled = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) return;
        if (intent.getAction().equals(ActionsPending.CANCEL_UPDATES)) {
            WorkManager.getInstance(context).cancelAllWorkByTag(ActionsPending.UPDATE_WORK_TAG);
            isWorkUpdatesLibraryCanceled = true;
        } else if (intent.getAction().equals(ActionsPending.CANCEL_DOWNLOADS)) {
            WorkManager.getInstance(context).cancelAllWorkByTag(ActionsPending.DOWNLOAD_CHAPTER_TAG);
            isWorkDownloadChaptersCanceled = true;
        }

    }

    public static boolean isIsWorkUpdatesLibraryCanceled() {
        if (isWorkUpdatesLibraryCanceled) {
            isWorkUpdatesLibraryCanceled = false;
            return true;
        }
        return false;
    }

    public static boolean isIsWorkDownloadChaptersCanceled() {
        if (isWorkDownloadChaptersCanceled) {
            return true;
        }
        return false;
    }
    public static void setIsWorkDownloadChaptersCanceled(){
        isWorkDownloadChaptersCanceled = false;
    }
}
