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
import android.util.TypedValue;
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
import androidx.core.content.res.ResourcesCompat;
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
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.trinity.Interfaces.Extensions;
import com.example.trinity.adapters.AdapterNavigation;
import com.example.trinity.databinding.ActivityMainBinding;
import com.example.trinity.extensions.MangaDexExtension;
import com.example.trinity.extensions.MangakakalotExtension;
import com.example.trinity.fragments.LibraryFragment;
import com.example.trinity.fragments.ReaderMangaFragment;
import com.example.trinity.fragments.UpdatesFragment;
import com.example.trinity.models.Model;
import com.example.trinity.preferecesConfig.ConfigClass;
import com.example.trinity.services.AniListApiRequester;
import com.example.trinity.services.ClearLogosTemp;
import com.example.trinity.services.ClearPageCacheWork;
import com.example.trinity.services.MangakakalotTagsSaver;
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

import org.json.JSONException;

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
    private ValueAnimator animatorHide;
    private ValueAnimator animatorShow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ConfigClass.ConfigTheme.setTheme(this);

//        binding.menuNavi.setBackgroundColor(getColor(R.color.FullRed));
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary,typedValue,false);
        binding.menuNavi.setBackgroundColor(typedValue.data);
        binding.menuNavi.setItemIconTintList(ResourcesCompat.getColorStateList(getResources(),R.color.item_bottom_navigation_color,getTheme()));
        binding.menuNavi.setItemTextColor(ResourcesCompat.getColorStateList(getResources(),R.color.item_bottom_navigation_color,getTheme()));
        sharedPreferences = getSharedPreferences(ConfigClass.TAG_PREFERENCE, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if (!sharedPreferences.getBoolean(ConfigClass.ConfigContent.ALREDY_LOADED_TAGS, false)) {
            new Thread() {
                @Override
                public void run() {
                    MangaDexExtension mangaDexExtension = new MangaDexExtension("", "");
                    ArrayList<TagManga> tags = mangaDexExtension.getTags();
                    model.saveTag(tags);
                }
            }.start();
        }

        mangasFromDataBaseViewModel = new ViewModelProvider(this).get(MangasFromDataBaseViewModel.class);
        new Thread() {
            @Override
            public void run() {
                model = Model.getInstance(MainActivity.this);
                LogoMangaStorage storage = new LogoMangaStorage(MainActivity.this);
                storage.createIfNotExistsFolderForLogos();

                LogoMangaStorageTemp storageTemp = new LogoMangaStorageTemp(MainActivity.this);
                storageTemp.createIfNotExistsFolderTempForLogos();

                MangakakalotTagsSaver mangakakalotTagsSaver = new MangakakalotTagsSaver(MainActivity.this);
                mangakakalotTagsSaver.saveIfNotExistsTags();

                dataSet = model.selectAllMangas(false,21,0);

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mangasFromDataBaseViewModel.setMangas(dataSet);
                    }
                });
            }
        }.start();

        MangaDataViewModel mangaDataViewModel = new ViewModelProvider(this).get(MangaDataViewModel.class);
        initNavigation();
        this.checkFolders();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void initNavigation() {

//        navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.hostFragmentMain);
//
//        assert navHost != null;
//        navController = navHost.getNavController();
//
//        NavigationUI.setupWithNavController(binding.menuNavi, navController);
        this.binding.hostFragmentMain.setAdapter(new AdapterNavigation(this));

        this.binding.menuNavi.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                System.out.println(item.get);
                if (item.getItemId() == R.id.library) {
                    binding.hostFragmentMain.setCurrentItem(0);
                } else if (item.getItemId() == R.id.updates) {
                    binding.hostFragmentMain.setCurrentItem(1);
                } else if (item.getItemId() == R.id.extensions) {
                    binding.hostFragmentMain.setCurrentItem(2);
                } else if (item.getItemId() == R.id.historyManga) {
                    binding.hostFragmentMain.setCurrentItem(3);
                } else if (item.getItemId() == R.id.settings) {
                    binding.hostFragmentMain.setCurrentItem(4);
                }
                return true;
            }
        });
        binding.hostFragmentMain.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        binding.menuNavi.setSelectedItemId(R.id.library);
                        break;
                    case 1:
                        binding.menuNavi.setSelectedItemId(R.id.updates);
                        break;
                    case 2:
                        binding.menuNavi.setSelectedItemId(R.id.extensions);
                        break;
                    case 3:
                        binding.menuNavi.setSelectedItemId(R.id.historyManga);
                        break;
                    default:
                        binding.menuNavi.setSelectedItemId(R.id.settings);
                        break;
                }
            }
        });
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
//        navController.popBackStack(R.id.library, false);
//        navController.navigate(R.id.library);
        binding.hostFragmentMain.setCurrentItem(0);
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

    public void backToUpdates() {
        navController.navigateUp();
    }

    public void checkFolders() {
        new Thread() {
            @Override
            public void run() {
                ChapterStorageManager chapterStorageManager = new ChapterStorageManager(MainActivity.this);
                chapterStorageManager.createIfNotExistsFolderForChapters();

                PageCacheManager pageCacheManager = PageCacheManager.getInstance(MainActivity.this);
                pageCacheManager.createIfNotExistCacheChapterFolder();

            }
        }.start();
        WorkManager workManager = WorkManager.getInstance(this);
        OneTimeWorkRequest workRequest1 = new OneTimeWorkRequest.Builder(ClearPageCacheWork.class).build();
        OneTimeWorkRequest workRequest2 = new OneTimeWorkRequest.Builder(ClearLogosTemp.class).build();
        workManager.beginWith(workRequest1).then(workRequest2).enqueue();
    }

    public void hideBottomNavigation(){
        if(!isBottomShowed || animatorShow.isRunning() || animatorHide.isRunning()){
            return;
        }
        isBottomShowed = false;

        animatorHide.addUpdateListener((animation)->{
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) binding.menuNavi.getLayoutParams();
            layoutParams.setMargins(0,0,0,(int) animation.getAnimatedValue());
            binding.menuNavi.setLayoutParams(layoutParams);
        });

        animatorHide.start();
    }
    public void showBottomNavigation(){
        if(isBottomShowed || animatorHide.isRunning() || animatorHide.isRunning()){
            return;
        }
        isBottomShowed = true;

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) binding.menuNavi.getLayoutParams();
        animatorHide.addUpdateListener((animation)->{
            layoutParams.setMargins(0,0,0,(int) animation.getAnimatedValue());
            binding.menuNavi.setLayoutParams(layoutParams);
        });
        animatorHide.start();
    }

}