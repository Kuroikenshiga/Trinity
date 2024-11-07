package com.example.trinity;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestListener;
import com.example.trinity.Interfeces.Extensions;
import com.example.trinity.adapters.AdapterMangas;

import com.example.trinity.databinding.ActivityExtensionShowContentBinding;
import com.example.trinity.extensions.MangaDexExtension;
import com.example.trinity.fragments.LibraryFragment;
import com.example.trinity.models.Model;
import com.example.trinity.preferecesConfig.ConfigClass;
import com.example.trinity.services.ClearLogosTemp;
import com.example.trinity.services.ClearPageCacheWork;
import com.example.trinity.storageAcess.LogoMangaStorageTemp;
import com.example.trinity.valueObject.ChapterManga;
import com.example.trinity.valueObject.Manga;
import com.example.trinity.valueObject.TagManga;


import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ExtensionShowContentActivity extends AppCompatActivity {
    private String language;

    private ActivityExtensionShowContentBinding binding;
    private boolean searchIsShowed = false;

    private boolean supressManyLoad = true;
    private boolean controllClick = false;
    private Handler mainHandler;
    private MangaDexExtension mangaDexExtension;
    private ArrayList<Manga> mangaListed;
    private AdapterMangas adapter;
    private int controllLoad = 0;

    private boolean isAdvancedSearchShow = false;
    private boolean isAdvancedSearchSettingsChanged = false;
    private Thread workerThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityExtensionShowContentBinding.inflate(getLayoutInflater());
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(searchIsShowed){
                    hiddenSearch();
                    return;
                }
                ExtensionShowContentActivity.this.finish();


            }
        };

        getOnBackPressedDispatcher().addCallback(this,callback);


        new Thread(){
            @Override
            public void run(){
                Model model = Model.getInstance(ExtensionShowContentActivity.this);
                ArrayList<TagManga> tags = model.selectAllTags();
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                for(TagManga t:tags){
                    CheckBox checkBox = new CheckBox(ExtensionShowContentActivity.this);
                    checkBox.setLayoutParams(lp);
                    checkBox.setText(t.getNome());
                    checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
                    checkBox.setTextColor(getColor(R.color.white));
                    checkBox.setTag(t.getId());
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            isAdvancedSearchSettingsChanged = true;
                        }
                    });
                    ExtensionShowContentActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.tagsContainer.addView(checkBox);
                        }
                    });
                }
            }
        }.start();
        binding.showAdvancedSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValueAnimator animator = isAdvancedSearchShow?ValueAnimator.ofInt(0,-400):ValueAnimator.ofInt(-400,0);
                animator.setDuration(500);

                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) binding.configSearch.getLayoutParams();
                        lp.bottomMargin = (int)((int)animation.getAnimatedValue()*getResources().getDisplayMetrics().density);
                        binding.configSearch.setLayoutParams(lp);
                    }
                });
                ValueAnimator animatorRotate = !isAdvancedSearchShow?ValueAnimator.ofInt(180,0):ValueAnimator.ofInt(0,180);
                animatorRotate.setDuration(500);
                animatorRotate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                        binding.showAdvancedSearch.setRotation((int)animation.getAnimatedValue());
                    }
                });
                animator.start();
                animatorRotate.start();
                if(isAdvancedSearchShow && isAdvancedSearchSettingsChanged){
                    if(workerThread != null && workerThread.isAlive()){
                        workerThread.interrupt();
                    }
                    workerThread = new Thread(){
                        @Override
                        public void run(){
                            ArrayList<String>tags = new ArrayList<>();
                            for(int i = 0; i < binding.tagsContainer.getChildCount();i++){
                                CheckBox checkBox = (CheckBox) binding.tagsContainer.getChildAt(i);
                                if(checkBox.isChecked()){
                                    tags.add((String) checkBox.getTag());
                                }

                            }
                            ExtensionShowContentActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    int itemCount = mangaListed.size();
                                    mangaListed.clear();
                                    adapter.notifyDataSetChanged();
                                    binding.progressTop.setVisibility(View.VISIBLE);
                                }
                            });
                            mangaDexExtension.addTags(tags);
                            mangaDexExtension.updates(mainHandler);
                        }
                    };
                    workerThread.start();
                }
                isAdvancedSearchShow = !isAdvancedSearchShow;
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences(ConfigClass.TAG_PREFERENCE,MODE_PRIVATE);
        String imageQuality = sharedPreferences.getString(ConfigClass.ConfigContent.IMAGE_QUALITY,"dataSaver");
        setContentView(binding.getRoot());

        binding.logoHeader.setImageDrawable(getResources().getDrawable(getIntent().getIntExtra("Logo", 0)));
        binding.textHeader.setText(getIntent().getStringExtra("Titulo"));
        this.language = getIntent().getStringExtra("Language");

        binding.backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExtensionShowContentActivity.this.finish();
            }
        });

        mangaListed = new ArrayList<>();
         adapter = new AdapterMangas(ExtensionShowContentActivity.this, mangaListed,this.language);
         adapter.setFromUpdates(true);
        startUphandler();
        binding.search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExtensionShowContentActivity.this,SearchResultActivity.class);
                intent.putExtra("language",language);
                intent.putExtra("SearchField",binding.searchField.getText().toString());
                ExtensionShowContentActivity.this.startActivity(intent);
            }
        });
        binding.showSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearch();
            }
        });
        RecyclerView recyclerView = binding.reciclerViewMangas;
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(ExtensionShowContentActivity.this, 3));
        recyclerView.setHasFixedSize(false);

        mangaDexExtension = new MangaDexExtension(this.language,imageQuality);

       workerThread = new Thread(){
            public void run(){
                mangaDexExtension.updates(mainHandler);
            }
        };
        workerThread.start();

        loadMoreContent();
        onClickEnter();
    }

    @Override
    public void onDestroy() {
//        Glide.with(this).clearOnStop();

        WorkRequest workRequest = new OneTimeWorkRequest.Builder(ClearLogosTemp.class).build();
        WorkManager.getInstance(getApplicationContext()).enqueue(workRequest);
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
       // System.out.println("Destruiu");

    }
    @Override
    public void onResume(){
        super.onResume();
        this.controllClick = false;
    }
    public void showSearch() {

        searchIsShowed = true;
        binding.headerContainer1.setVisibility(View.VISIBLE);
        ValueAnimator show = ValueAnimator.ofInt(-60, 0);
        show.setDuration(500);

        show.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams) binding.headerContainer1.getLayoutParams();
                lp1.topMargin = (int)(((int)animation.getAnimatedValue())*getResources().getDisplayMetrics().density);
                binding.headerContainer1.setLayoutParams(lp1);
            }
        });
        show.start();
    }
    public void hiddenSearch() {
        searchIsShowed = false;

        ValueAnimator hidden = ValueAnimator.ofInt(0, -60);
        hidden.setDuration(500);

        hidden.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams) binding.headerContainer1.getLayoutParams();
                lp1.topMargin = (int)(((int)animation.getAnimatedValue())*getResources().getDisplayMetrics().density);
                binding.headerContainer1.setLayoutParams(lp1);
            }
        });
        hidden.start();
//        Timer t = new Timer();
//        t.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                ExtensionShowContentActivity.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        binding.headerContainer1.setVisibility(View.GONE);
//                    }
//                });
//            }
//        },400);
    }
    private void onClickEnter(){
        binding.searchField.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER && !controllClick){
                    controllClick = true;
                    binding.search.performClick();
                    if(workerThread != null && workerThread.isAlive()){
                        workerThread.interrupt();
                    }
                    workerThread = new Thread(){
                        public void run(){
                            mangaDexExtension.updates(mainHandler);
                        }
                    };
                    workerThread.start();
                    return true;
                }
                return false;
            }
        });

    }
    private void loadMoreContent(){
        binding.reciclerViewMangas.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(!recyclerView.canScrollVertically(1) && !supressManyLoad){

                    supressManyLoad = true;
                    workerThread = new Thread(){
                        @Override
                        public void run(){
                            ExtensionShowContentActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    binding.progressBotton.setVisibility(View.VISIBLE);
                                }
                            });
                            mangaDexExtension.updates(mainHandler);
                        }
                    };
                    workerThread.start();
                }
            }
        });


    }
    private void startUphandler(){
        mainHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg){
                //System.out.println("Dados: "+msg.getData().get("dados"));

                if(msg.what == 1){
                    isAdvancedSearchSettingsChanged = false;
                    binding.progressTop.setVisibility(View.GONE);
                    binding.progressBotton.setVisibility(View.GONE);
                    mangaListed.add(msg.getData().getParcelable("dados"));

                    adapter.notifyItemInserted(mangaListed.size()-1);

//                    binding.progressTop.setVisibility(View.GONE);
//                    binding.progressBotton.setVisibility(View.GONE);
                    controllLoad++;
                    if(controllLoad == 27){
                        supressManyLoad = false;
                        controllLoad = 0;
                    }

                }

                else if(msg.what == 2){
                    binding.progressTop.setVisibility(View.GONE);
                    binding.progressBotton.setVisibility(View.GONE);
                    supressManyLoad = false;
                }

            }
        };
    }

}

