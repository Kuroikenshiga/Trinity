package com.example.trinity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavHost;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trinity.adapters.AdapterChapters;
import com.example.trinity.adapters.AdapterGenres;
import com.example.trinity.databinding.ActivityMangaShowContentBinding;

import com.example.trinity.fragments.ReaderMangaFragment;
import com.example.trinity.models.Model;
import com.example.trinity.valueObject.ChapterManga;
import com.example.trinity.extensions.MangaDexExtension;
import com.example.trinity.valueObject.Manga;
import com.example.trinity.utilities.OrderEnum;
import com.example.trinity.utilities.SortUtilities;

import com.example.trinity.viewModel.MangaDataViewModel;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class MangaShowContentActivity extends AppCompatActivity {
    private ActivityMangaShowContentBinding binding;
    private Manga manga;
    private MangaDataViewModel mangaDataViewModel;
    private NavController navController;
    private NavHostFragment navHostFragment;
    public boolean fromMain;
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE_CODE = 2;
    private String extension;
    private boolean isDownloadButtonShow = false;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMangaShowContentBinding.inflate(getLayoutInflater());
        extension = getIntent().getStringExtra("Extension");
        setContentView(binding.getRoot());
        this.manga = getIntent().getParcelableExtra("Item");

        mangaDataViewModel = new ViewModelProvider(this).get(MangaDataViewModel.class);

        fromMain =  getIntent().getBooleanExtra("FromMain",false);
        mangaDataViewModel.setManga(manga);
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHost);
        navController = navHostFragment.getNavController();

//        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }
    public void backToInfoManga(){
        navController.navigateUp();
    }
    public String getExtension(){
        return this.extension;
    }
}