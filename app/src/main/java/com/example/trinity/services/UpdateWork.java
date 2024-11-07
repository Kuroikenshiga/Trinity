package com.example.trinity.services;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.trinity.R;
import com.example.trinity.extensions.MangaDexExtension;
import com.example.trinity.fragments.UpdatesFragment;
import com.example.trinity.models.Model;

import com.example.trinity.preferecesConfig.ConfigClass;
import com.example.trinity.services.broadcasts.ActionsPending;
import com.example.trinity.services.broadcasts.CancelCurrentWorkReceiver;
import com.example.trinity.valueObject.ChapterManga;
import com.example.trinity.valueObject.ChapterUpdated;
import com.example.trinity.valueObject.Manga;
import com.example.trinity.viewModel.UpdatesViewModel;

import java.util.ArrayList;

public class UpdateWork extends Worker {
    private Context context;

    private NotificationChannel notificationChannel;
    private NotificationManagerCompat notificationManager;
    private final static String CHANNEL_NOTIFICATION = "Atualizando Biblioteca";
    private final static String CHANNEL_NOTIFICATION_ID = "CHANNEL1";
    private final int NOTIFICATION_ID = 1;
    private boolean haveNewChapters = false;
    private ArrayList<ChapterUpdated> dataSet;
    private boolean isAlredyChecked = false;
    public static final String WORK_NAME = "WorkUpdateLibrary";
    private Uri sound;
    public UpdateWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;

    }

    @NonNull
    @Override
    public Result doWork() {
        this.createChannerNotification();
        Model model = Model.getInstance(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences(ConfigClass.TAG_PREFERENCE, Context.MODE_PRIVATE);
        String imageQuality = sharedPreferences.getString(ConfigClass.ConfigContent.IMAGE_QUALITY,"dataSaver");

        Intent cancelUpdateIntent = new Intent(context, CancelCurrentWorkReceiver.class);
        cancelUpdateIntent.setAction(ActionsPending.CANCEL_UPDATES);

        PendingIntent cancelPending = PendingIntent.getBroadcast(context,0,cancelUpdateIntent,PendingIntent.FLAG_IMMUTABLE);

        ArrayList<Manga> mangas = model.selectAllMangas(true);
        MangaDexExtension mangaDexExtension = new MangaDexExtension("",imageQuality);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_NOTIFICATION_ID)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle("Atualizando biblioteca ("+1+" de "+mangas.size()+")")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setProgress(mangas.size(), 0, false)
                .addAction(R.drawable.cancel_work_shape,"Cancelar atualização", cancelPending);


        this.notify(notification);
        int progress = 0;
        for (Manga m : mangas) {
            isAlredyChecked = false;
//            System.out.println(m.getTitulo());
            notification.setContentTitle("Atualizando biblioteca ("+(progress+1)+" de "+mangas.size()+")");
            notification.setContentText(m.getTitulo());
            this.notify(notification);

            if(CancelCurrentWorkReceiver.isIsWorkUpdatesLibraryCanceled()){
                break;
            }
            mangaDexExtension.setLanguage(m.getLanguage());
            ArrayList<ChapterManga> chaptersFromApi = mangaDexExtension.viewChapters(m.getId(), null, null);
            double lastChapter = mangaDexExtension.getMangaStatus(m.getId());
            if(lastChapter != 0){
                model.setLastChapterManga(m.getId(),m.getLanguage(),lastChapter);
            }
            for (ChapterManga chapterManga : chaptersFromApi) {
                if (!m.isChapterAlredySaved(chapterManga.getId())) {
                    ChapterUpdated chapterUpdated = new ChapterUpdated(m, chapterManga);
                    model.addNewChapter(chapterUpdated);
                    this.haveNewChapters = true;
                }
            }

            progress++;
            notification.setProgress(mangas.size(), progress, false);

            this.notify(notification);
        }

        notification = new NotificationCompat.Builder(context, CHANNEL_NOTIFICATION_ID)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle("Biblioteca atualizada")
                .setContentText(haveNewChapters?"Há novos capítulos para leitura":"Não há novos capítulos para leitura")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setProgress(0, 0, false);


        this.notify(notification);
        return Result.success();
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
    public void setCancelPending(PendingIntent p){}



}
