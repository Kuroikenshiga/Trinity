package com.example.trinity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.trinity.Interfeces.Extensions;
import com.example.trinity.adapters.AdapterMangas;
import com.example.trinity.databinding.ActivitySearchResultBinding;
import com.example.trinity.extensions.MangaDexExtension;
import com.example.trinity.extensions.MangakakalotExtension;
import com.example.trinity.preferecesConfig.ConfigClass;
import com.example.trinity.valueObject.Manga;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SearchResultActivity extends AppCompatActivity {
    private String language;
    private ActivitySearchResultBinding binding;
    private boolean supressManyLoad = true;
    private Extensions mangaDexExtension;
    private boolean controllEnter = false;

    Thread workerThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ConfigClass.ConfigTheme.setTheme(this);

        binding = ActivitySearchResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        this.language = getIntent().getStringExtra("language");
        this.binding.searchField.setText(getIntent().getStringExtra("SearchField"));
        SharedPreferences sharedPreferences = getSharedPreferences(ConfigClass.TAG_PREFERENCE,MODE_PRIVATE);
        String imageQuality = sharedPreferences.getString(ConfigClass.ConfigContent.IMAGE_QUALITY,"dataSaver");

        mangaDexExtension = getIntent().getStringExtra("Extension").equals(Extensions.MANGADEX)?new MangaDexExtension(language,imageQuality):new MangakakalotExtension(null);
        final ArrayList<Manga>[] mangaListedModelsAll = new ArrayList[]{new ArrayList()};
        AdapterMangas adapter = new AdapterMangas(SearchResultActivity.this, mangaListedModelsAll[0],this.language);
        adapter.setShowLanguageIcon(true);
        adapter.setFromUpdates(true);
        final int[] controllLoad = {0};
        binding.reciclerViewMangas.setLayoutManager(new GridLayoutManager(this,3));
        binding.reciclerViewMangas.setAdapter(adapter);

        Handler mainHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg){
//                System.out.println("Dados: "+msg.getData().get("dados"));

                if(msg.what == Extensions.RESPONSE_ITEM){
                    controllEnter = false;
                    mangaListedModelsAll[0].add(msg.getData().getParcelable("dados"));
                    adapter.notifyDataSetChanged();
                    binding.progressTop.setVisibility(View.GONE);

                    controllLoad[0]++;
                    if(controllLoad[0] == 15){
                        supressManyLoad = false;
                        controllLoad[0] = 0;
                        binding.progressBotton.setVisibility(View.GONE);
                    }

                }

                else if(msg.what == Extensions.RESPONSE_FINAL){
                    binding.progressTop.setVisibility(View.GONE);
                    supressManyLoad = false;

                }
                else if(msg.what == Extensions.RESPONSE_EMPTY){
                    binding.resultContainer.setVisibility(View.GONE);
                    binding.ErrorOnResult.setVisibility(View.VISIBLE);
                    binding.errorText.setText("Nenhum resultado encontrado para "+binding.searchField.getText().toString().toString());
                }

            }
        };
        workerThread = new Thread(){
            @Override
            public void run(){
                mangaDexExtension.search(binding.searchField.getText().toString(),mainHandler);
            }
        };
        workerThread.start();
//        binding.search.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(workerThread != null && workerThread.isAlive()){
//                    workerThread.interrupt();
//                }
//                mangaDexExtension = new MangaDexExtension(language,imageQuality);
//                mangaListedModelsAll[0].clear();
//                adapter.notifyDataSetChanged();
//                binding.progressTop.setVisibility(View.VISIBLE);
//                workerThread = new Thread(){
//                    @Override
//                    public void run(){
//                        mangaDexExtension.search(binding.searchField.getText().toString(),mainHandler);
//                    }
//                };
//                workerThread.start();
//                binding.progressTop.setVisibility(View.VISIBLE);
//            }
//        });


        binding.reciclerViewMangas.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(!recyclerView.canScrollVertically(1) && !supressManyLoad){
//                    System.out.println("Entrou");
                    supressManyLoad = true;
                    new Thread(){
                        @Override
                        public void run(){

                            SearchResultActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    binding.progressBotton.setVisibility(View.VISIBLE);
                                }
                            });
                            mangaDexExtension.search(binding.searchField.getText().toString(),mainHandler);
                        }
                    }.start();
                }
            }
        });
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        binding.searchAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(workerThread != null && workerThread.isAlive()){
                    workerThread.interrupt();
                }
                mangaDexExtension = new MangaDexExtension(language,imageQuality);
                mangaListedModelsAll[0].clear();
                adapter.notifyDataSetChanged();
                binding.progressTop.setVisibility(View.VISIBLE);
                workerThread = new Thread(){
                    @Override
                    public void run(){
                        mangaDexExtension.search(binding.searchField.getText().toString(),mainHandler);
                    }
                };
                workerThread.start();
                binding.progressTop.setVisibility(View.VISIBLE);
            }
        });
        binding.searchField.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER){
                    mangaDexExtension = new MangaDexExtension(language,imageQuality);
                  
                   if(!controllEnter){
                       controllEnter = true;
                       binding.progressTop.setVisibility(View.VISIBLE);
                       workerThread = new Thread(){
                           @Override
                           public void run(){
                               mangaDexExtension.search(binding.searchField.getText().toString(),mainHandler);
                           }
                       };
                       workerThread.start();

                   }
                   mangaListedModelsAll[0].clear();
                   adapter.notifyDataSetChanged();
                   controllLoad[0] = 0;

                    return true;
                }
                return false;
            }
        });
    }
    public void stopLoading(){
        binding.progressBotton.setVisibility(View.GONE);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
//        binding.reciclerViewMangas.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
//            @Override
//            public void onViewAttachedToWindow(@NonNull View v) {
//
//            }
//
//            @Override
//            public void onViewDetachedFromWindow(@NonNull View v) {
//                Glide.with(v.getContext()).clearOnStop();
//            }
//        });
        if(workerThread != null && workerThread.isAlive()){
            workerThread.interrupt();
            workerThread = null;
        }
    }
}