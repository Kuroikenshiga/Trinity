package com.example.trinity.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.trinity.R;
import com.example.trinity.extensions.MangaDexExtension;
import com.example.trinity.models.Model;
import com.example.trinity.preferecesConfig.ConfigClass;
import com.example.trinity.services.broadcasts.ActionsPending;
import com.example.trinity.services.broadcasts.CancelCurrentWorkReceiver;
import com.example.trinity.storageAcess.ChapterStorageManager;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;

public class DownloadChapterWork extends Worker {
    private Context context;

    private NotificationChannel notificationChannel;
    private NotificationManagerCompat notificationManager;
    private final static String CHANNEL_NOTIFICATION = "Download de capítulos";
    private final static String CHANNEL_NOTIFICATION_ID = "CHANNEL2";
    private final int NOTIFICATION_ID = 1;
    private Model model;
    public DownloadChapterWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        model = Model.getInstance(context);
    }
    @NonNull
    @Override
    public Result doWork() {

        this.createChannerNotification();
        String[] ids = getInputData().getStringArray("chaptersID");
        String language = getInputData().getString("language");
        String mangaApiID = getInputData().getString("folderChapterName");
        ArrayList<String> returnValue = new ArrayList<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences(ConfigClass.TAG_PREFERENCE,Context.MODE_PRIVATE);
        String imageQuality = sharedPreferences.getString(ConfigClass.ConfigContent.IMAGE_QUALITY,"dataSaver");
        assert ids != null;
        Intent cancelDownloadIntent = new Intent(context, CancelCurrentWorkReceiver.class);
        cancelDownloadIntent.setAction(ActionsPending.CANCEL_DOWNLOADS);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,1,cancelDownloadIntent,PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notifiBuilder = new NotificationCompat.Builder(context,CHANNEL_NOTIFICATION_ID)
                .setContentTitle("Baixando capítulos ("+1+" de "+ids.length+")")
                .setSmallIcon(R.drawable.app_icon)
                .setProgress(ids.length,0,false)
                .addAction(R.drawable.cancel_work_shape,"Cancelar atualização", pendingIntent);

        this.notify(notifiBuilder);
        String folderName = mangaApiID+"_"+language;

        ChapterStorageManager storageManager = new ChapterStorageManager(context);
        File folderChaptersPath = storageManager.createIfNotExistFolderMangaChapters(folderName);

        MangaDexExtension mangaDexExtension = new MangaDexExtension(language,imageQuality);
        int indexChapters = 0;
        for(String s:ids){
            int index = 1;
//            System.out.println(s);
            if(s == null){
                break;
            }
            Bundle bundle = mangaDexExtension.getChapterPages(s);

            if(bundle==null){
                CancelCurrentWorkReceiver.setIsWorkDownloadChaptersCanceled();
                Data data = new Data.Builder().putStringArray("ids",new String[]{}).build();
                return Result.failure(data);
            }
            Bitmap[] bitmaps = mangaDexExtension.loadChapterPages(bundle.getStringArray("imgs"),bundle.getString("hash"),bundle.getString("baseUrl"));

            if(CancelCurrentWorkReceiver.isIsWorkDownloadChaptersCanceled()){
                Data data = new Data.Builder().putStringArray("ids",returnValue.toArray(new String[0])).build();
                notifiBuilder.setProgress(0,0,false);
                notifiBuilder.setContentTitle("Download "+(ids.length > 1?"do capítulo":"dos capítulos")+" cancelado😢");
                notifiBuilder.setContentText((ids.length - returnValue.size())+(ids.length - returnValue.size() > 1?" capítulos":" capítulo")+" não "+(ids.length - returnValue.size() > 1?"foram baixados":"foi baixado"));
                notify(notifiBuilder);
                CancelCurrentWorkReceiver.setIsWorkDownloadChaptersCanceled();
                return Result.success(data);
            }
            for(int i = 1; i < bitmaps.length - 1;i++){
                Bitmap bitmap = bitmaps[i];
                String fileName = Integer.toString(index)+"-"+Long.toString(Instant.now().toEpochMilli())+".jpeg";
                String imagePath = storageManager.saveChapterPage(bitmap,fileName,folderChaptersPath,fileName);
                if(!imagePath.isEmpty()){
                    model.savePage(s,imagePath,i);
                }
                index++;
            }
            notifiBuilder.setContentTitle("Baixando capítulos ("+(indexChapters+1)+" de "+ids.length+")");
            indexChapters++;
            notifiBuilder.setProgress(ids.length,indexChapters,false);
            this.notify(notifiBuilder);
            returnValue.add(s);
        }

        notifiBuilder.setProgress(0,0,false);
        notifiBuilder.setContentTitle("Download dos capítulos concluído😉");
        notifiBuilder.setContentText(ids.length+" capítulos baixados com sucesso");
        notify(notifiBuilder);
        Data data = new Data.Builder().putStringArray("ids",ids).build();
        CancelCurrentWorkReceiver.setIsWorkDownloadChaptersCanceled();
        return Result.success(data);
    }
    private void createChannerNotification() {
        this.notificationChannel = new NotificationChannel(CHANNEL_NOTIFICATION_ID, CHANNEL_NOTIFICATION, NotificationManager.IMPORTANCE_LOW);
        this.notificationChannel.setLightColor(Color.BLUE);
        notificationChannel.setSound(null, null);
        this.notificationManager = NotificationManagerCompat.from(context);
        this.notificationManager.createNotificationChannel(this.notificationChannel);

    }

    public void notify(NotificationCompat.Builder n) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            this.notificationManager.notify(this.NOTIFICATION_ID, n.build());


        }


    }
}
