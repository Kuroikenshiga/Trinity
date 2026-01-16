package com.example.trinity;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trinity.Interfaces.Extensions;
import com.example.trinity.adapters.AdapterMangas;
import com.example.trinity.databinding.ActivitySearchResultBinding;
import com.example.trinity.extensions.MangaDexExtension;
import com.example.trinity.extensions.MangaLivreExtension;
import com.example.trinity.extensions.MangakakalotExtension;
import com.example.trinity.preferecesConfig.ConfigClass;
import com.example.trinity.valueObject.Manga;

import java.util.ArrayList;

public class SearchResultActivity extends AppCompatActivity {
    private String language;
    private ActivitySearchResultBinding binding;
    private boolean supressManyLoad = true;
    private Extensions mangaDexExtension;
    private boolean controllEnter = false;
    private int responseSize = 15;
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
//        System.out.println("-------------"+getIntent().getStringExtra("Extension"));
//        mangaDexExtension = getIntent().getStringExtra("Extension").equals(Extensions.MANGADEX)?new MangaDexExtension(language,imageQuality):getIntent().getStringExtra("Extension").equals(Extensions.MANGALIVRE)?new MangaLivreExtension(null) :new MangakakalotExtension(null);
        mangaDexExtension = getIntent().getParcelableExtra("Extension");
        final ArrayList<Manga>[] mangaListedModelsAll = new ArrayList[]{new ArrayList()};
        AdapterMangas adapter = new AdapterMangas(SearchResultActivity.this, mangaListedModelsAll[0],this.language);
        adapter.setExtension(mangaDexExtension);
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
                    if(controllLoad[0] == responseSize){
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
                else if(msg.what == Extensions.RESPONSE_CLOSE_CALLS){
                    binding.progressTop.setVisibility(View.GONE);
                    supressManyLoad = true;
                }
                else if(msg.what == Extensions.RESPONSE_SIZE){
                    responseSize = msg.getData().getInt("size_response");
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

        binding.searchAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(workerThread != null && workerThread.isAlive()){
                    workerThread.interrupt();
                }
//                mangaDexExtension = new MangaDexExtension(language,imageQuality);
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
//                    mangaDexExtension = new MangaDexExtension(language,imageQuality);
                  
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

        if(workerThread != null && workerThread.isAlive()){
            workerThread.interrupt();
            workerThread = null;
        }
    }
}