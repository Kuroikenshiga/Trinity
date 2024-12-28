package com.example.trinity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.navigation.NavArgument;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.navigation.Navigation;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.trinity.databinding.ActivityMainBinding;
import com.example.trinity.extensions.MangaDexExtension;
import com.example.trinity.fragments.LibraryFragment;
import com.example.trinity.fragments.ReaderMangaFragment;
import com.example.trinity.fragments.UpdatesFragment;
import com.example.trinity.models.Model;
import com.example.trinity.preferecesConfig.ConfigClass;
import com.example.trinity.services.ClearLogosTemp;
import com.example.trinity.services.ClearPageCacheWork;
import com.example.trinity.storageAcess.ChapterStorageManager;
import com.example.trinity.storageAcess.LogoMangaStorage;
import com.example.trinity.storageAcess.LogoMangaStorageTemp;
import com.example.trinity.storageAcess.PageCacheManager;
import com.example.trinity.valueObject.Manga;
import com.example.trinity.valueObject.TagManga;
import com.example.trinity.viewModel.MangaDataViewModel;
import com.example.trinity.viewModel.MangasFromDataBaseViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private boolean isBottomShowed = true;
    private Model model;
    public boolean isInFirstDestination = true;
    public boolean isInReadFragment = false;
    private NavHostFragment navHost;
    private NavController navController;
    private ArrayList<Manga> dataSet;
    public final int REQUEST_POST_NOTIFICATIONS_CODE = 1;
    private MangasFromDataBaseViewModel mangasFromDataBaseViewModel;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private NotificationCompat.Builder notification;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ConfigClass.ConfigTheme.setTheme(this);


        sharedPreferences = getSharedPreferences(ConfigClass.TAG_PREFERENCE,MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if(!sharedPreferences.getBoolean(ConfigClass.ConfigContent.ALREDY_LOADED_TAGS,false)){
            new Thread(){
                @Override
                public void run(){

                    MangaDexExtension mangaDexExtension = new MangaDexExtension("","");
                    ArrayList<TagManga> tags = mangaDexExtension.getTags();
                    model.saveTag(tags);
//                    System.out.println("Fazendo requisição das tags");
                }
            }.start();
        }

         mangasFromDataBaseViewModel = new ViewModelProvider(this).get(MangasFromDataBaseViewModel.class);
        new Thread(){
            @Override
            public void run(){
                model = Model.getInstance(MainActivity.this);
                LogoMangaStorage storage = new LogoMangaStorage(MainActivity.this);
                storage.createIfNotExistsFolderForLogos();

                LogoMangaStorageTemp storageTemp = new LogoMangaStorageTemp(MainActivity.this);
                storageTemp.createIfNotExistsFolderTempForLogos();

                boolean migrated = sharedPreferences.getBoolean(ConfigClass.ConfigLogoMigration.ALREDY_MIGRATED,false);
//                model.removeImagesFromDataBase();
                //Retirar a migração após a atualização
                if(!migrated){
                    System.out.println("Entrou");
                    model.doUpdateLogos();
                }

                dataSet = model.selectAllMangas(false);


//                for(Manga manga:dataSet){
////                    model.updateLocalStorageOfLogos(manga.getId(),manga.getLanguage(),storage.insertLogoManga(manga.getImage(), manga.getId()));
//                }
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mangasFromDataBaseViewModel.setMangas(dataSet);
                    }
                });
            }
        }.start();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        MangaDataViewModel mangaDataViewModel = new ViewModelProvider(this).get(MangaDataViewModel.class);
        initNavigation();
        this.checkFolders();

    }
    @Override
    public void onResume(){
        super.onResume();

    }
    private void initNavigation() {

        navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.hostFragmentMain);

        assert navHost != null;
        navController = navHost.getNavController();

        NavigationUI.setupWithNavController(binding.menuNavi, navController);

    }

    public void hiddenBottomNavigator() {


        ValueAnimator anim = ValueAnimator.ofInt(0, binding.menuNaviContainer.getHeight());
        anim.setDuration(500);
        anim.setRepeatCount(0);

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                lp.bottomMargin = (int) animation.getAnimatedValue() * -1;
                binding.menuNaviContainer.setLayoutParams(lp);

            }
        });
        anim.start();


    }

    public void showBottomNavigator() {

        isBottomShowed = true;
        ValueAnimator anim = ValueAnimator.ofInt(binding.menuNaviContainer.getHeight(), 0);
        anim.setDuration(500);
        anim.setRepeatCount(0);

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                lp.bottomMargin = (int) animation.getAnimatedValue() * -1;
                binding.menuNaviContainer.setLayoutParams(lp);

            }
        });
        anim.start();


    }

    public void controllShowBottomNavigator(Fragment fragment) {
        if (fragment instanceof ReaderMangaFragment) {
            binding.menuNavi.setVisibility(View.GONE);
            return;
        }
        binding.menuNavi.setVisibility(View.VISIBLE);
    }

    public void navigateToUpdates() {
        Navigation.findNavController(binding.hostFragmentMain).navigate(R.id.action_readerMangaFragment2_to_updates);
    }

    public void navigateToLibrary() {
        navController.popBackStack(R.id.library, false);
        navController.navigate(R.id.library);
    }



    public void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_POST_NOTIFICATIONS_CODE);

        }


    }
    public void backToUpdates(){
        navController.navigateUp();
    }
    public void checkFolders(){
        new Thread(){
            @Override
            public void run(){
                ChapterStorageManager chapterStorageManager = new ChapterStorageManager(MainActivity.this);
                chapterStorageManager.createIfNotExistsFolderForChapters();

                PageCacheManager pageCacheManager =  PageCacheManager.getInstance(MainActivity.this);
                pageCacheManager.createIfNotExistCacheChapterFolder();

            }
        }.start();
        WorkManager workManager = WorkManager.getInstance(this);
        OneTimeWorkRequest workRequest1 = new OneTimeWorkRequest.Builder(ClearPageCacheWork.class).build();
        OneTimeWorkRequest workRequest2 = new OneTimeWorkRequest.Builder(ClearLogosTemp.class).build();
        workManager.beginWith(workRequest1).then(workRequest2).enqueue();
    }


}