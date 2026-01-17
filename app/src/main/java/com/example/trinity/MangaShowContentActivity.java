package com.example.trinity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.trinity.databinding.ActivityMangaShowContentBinding;

import com.example.trinity.preferecesConfig.ConfigClass;
import com.example.trinity.valueObject.Manga;

import com.example.trinity.viewModel.MangaDataViewModel;

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
    private boolean canFinishCurrentContext = false;

    public static final String TO_LOADING = "toLoading", TO_READER = "toReader", TO_INFO = "toInfo";

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ConfigClass.ConfigTheme.setTheme(this);

        binding = ActivityMangaShowContentBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        extension = getIntent().getStringExtra("Extension");

        this.manga = getIntent().getParcelableExtra("Item");

        mangaDataViewModel = new ViewModelProvider(this).get(MangaDataViewModel.class);

        fromMain =  getIntent().getBooleanExtra("FromMain",false);

        mangaDataViewModel.setManga(manga);
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHost);
        navController = navHostFragment.getNavController();

        toLoadingFragment();
//        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }
    public void backToInfoManga(){
        if(canFinishCurrentContext){
            this.finish();
            return;
        }

        navController.navigateUp();
    }
    public String getExtension(){
        return this.extension;
    }

    private void toLoadingFragment(){
        if(getIntent().getStringExtra("route") != null && getIntent().getStringExtra("idChap") != null && getIntent().getStringExtra("route").equals(TO_LOADING)){
            mangaDataViewModel.setIdChap(getIntent().getStringExtra("idChap"));
            navController.navigate(R.id.action_infoMangaFragment2_to_loadingFragment);
        }
    }
    public void toReaderFragment(){
        canFinishCurrentContext = true;
        navController.navigate(R.id.action_loadingFragment_to_readerMangaFragment);
    }

}