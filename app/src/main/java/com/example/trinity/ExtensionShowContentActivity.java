package com.example.trinity;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.trinity.Interfeces.Extensions;
import com.example.trinity.adapters.AdapterMangas;
import com.example.trinity.adapters.AdapterSearchItensSimpleResult;
import com.example.trinity.databinding.ActivityExtensionShowContentBinding;
import com.example.trinity.extensions.MangaDexExtension;
import com.example.trinity.extensions.MangakakalotExtension;
import com.example.trinity.models.Model;
import com.example.trinity.preferecesConfig.ConfigClass;
import com.example.trinity.services.AniListApiRequester;
import com.example.trinity.services.ClearLogosTemp;
import com.example.trinity.storageAcess.LogoMangaStorage;
import com.example.trinity.valueObject.TagManga;
import com.example.trinity.viewModel.MangasFromDataBaseViewModel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class ExtensionShowContentActivity extends AppCompatActivity {
    private String language;

    private ActivityExtensionShowContentBinding binding;
    private boolean searchIsShowed = false;

    private boolean supressManyLoad = true;
    private boolean controllClick = false;
    private Handler mainHandler;
    private Extensions mangaDexExtension;
    private ArrayList<String[]> data = new ArrayList<>();
    private AdapterMangas adapter;
    private int controllLoad = 0;
    private boolean alredyClicked = false;
    private boolean isAdvancedSearchShow = false;
    private boolean isAdvancedSearchSettingsChanged = false;
    private Thread workerThread;
    private MangasFromDataBaseViewModel mangasFromDataBaseViewModel;
    private AdapterSearchItensSimpleResult adapterSearchItensSimpleResult;
    private Timer timer;
    private boolean search_has_change = false;
    private boolean isSearching = false;
    private long last_time_key_pressed;
    private boolean isTimerCanceled = false;
    private AniListApiRequester aniListApiRequester;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        aniListApiRequester = new AniListApiRequester();

        ConfigClass.ConfigTheme.setTheme(this);

        EdgeToEdge.enable(this);
        binding = ActivityExtensionShowContentBinding.inflate(getLayoutInflater());
        mangasFromDataBaseViewModel = new ViewModelProvider(this).get(MangasFromDataBaseViewModel.class);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchIsShowed) {
                    hiddenSearch();
                    return;
                }
                ExtensionShowContentActivity.this.finish();
            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);


        new Thread() {
            @Override
            public void run() {
                new LogoMangaStorage(ExtensionShowContentActivity.this).createIfNotExistsFolderForLogos();
                Model model = Model.getInstance(ExtensionShowContentActivity.this);
                ArrayList<TagManga> tags = model.selectAllTags();
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                TypedValue typedValue = new TypedValue();
                getTheme().resolveAttribute(com.google.android.material.R.attr.colorTertiary, typedValue, true);
                for (TagManga t : tags) {
                    CheckBox checkBox = getCheckBox(t, lp, typedValue);
                    ExtensionShowContentActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.tagsContainer.addView(checkBox);
                        }
                    });
                }
            }

            private @NonNull CheckBox getCheckBox(TagManga t, LinearLayout.LayoutParams lp, TypedValue typedValue) {
                CheckBox checkBox = new CheckBox(ExtensionShowContentActivity.this);
                checkBox.setLayoutParams(lp);
                checkBox.setText(t.getNome());
                checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                checkBox.setTextColor(typedValue.data);
                checkBox.setTag(t.getId());
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        isAdvancedSearchSettingsChanged = true;
                    }
                });
                return checkBox;
            }
        }.start();
        binding.showAdvancedSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValueAnimator animator = isAdvancedSearchShow ? ValueAnimator.ofInt(0, -400) : ValueAnimator.ofInt(-400, 0);
                animator.setDuration(500);

                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) binding.configSearch.getLayoutParams();
                        lp.bottomMargin = (int) ((int) animation.getAnimatedValue() * getResources().getDisplayMetrics().density);
                        binding.configSearch.setLayoutParams(lp);
                    }
                });
                ValueAnimator animatorRotate = !isAdvancedSearchShow ? ValueAnimator.ofInt(180, 0) : ValueAnimator.ofInt(0, 180);
                animatorRotate.setDuration(500);
                animatorRotate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                        binding.showAdvancedSearch.setRotation((int) animation.getAnimatedValue());
                    }
                });
                animator.start();
                animatorRotate.start();
                if (isAdvancedSearchShow && isAdvancedSearchSettingsChanged) {
                    if (workerThread != null && workerThread.isAlive()) {
                        workerThread.interrupt();
                    }
                    workerThread = new Thread() {
                        @Override
                        public void run() {
                            ArrayList<String> tags = new ArrayList<>();
                            for (int i = 0; i < binding.tagsContainer.getChildCount(); i++) {
                                CheckBox checkBox = (CheckBox) binding.tagsContainer.getChildAt(i);
                                if (checkBox.isChecked()) {
                                    tags.add((String) checkBox.getTag());
                                }

                            }
                            ExtensionShowContentActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mangasFromDataBaseViewModel.getMangas().clear();
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
        adapterSearchItensSimpleResult = new AdapterSearchItensSimpleResult(this, this.data,binding.searchField);
        binding.searchItens.setAdapter(adapterSearchItensSimpleResult);
        binding.searchItens.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        SharedPreferences sharedPreferences = getSharedPreferences(ConfigClass.TAG_PREFERENCE, MODE_PRIVATE);
        String imageQuality = sharedPreferences.getString(ConfigClass.ConfigContent.IMAGE_QUALITY, "dataSaver");
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

        adapter = new AdapterMangas(ExtensionShowContentActivity.this, mangasFromDataBaseViewModel.getMangas(), this.language);
        adapter.setFromUpdates(true);
        startUphandler();

        binding.searchAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExtensionShowContentActivity.this, SearchResultActivity.class);
                intent.putExtra("language", language);
                intent.putExtra("SearchField", binding.searchField.getText().toString());
                intent.putExtra("Extension", mangaDexExtension instanceof MangaDexExtension ? Extensions.MANGADEX : Extensions.MANGAKAKALOT);
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

        mangaDexExtension = getIntent().getStringExtra("Extension").equals(Extensions.MANGADEX) ? new MangaDexExtension(this.language, imageQuality) : new MangakakalotExtension(null);

        workerThread = new Thread(()->mangaDexExtension.updates(mainHandler));
        workerThread.start();

        binding.searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                search_has_change = true;
                last_time_key_pressed = Instant.now().toEpochMilli();
                if(s.length() == 0){
                    binding.loading.setVisibility(View.GONE);
                    return;
                }
                binding.loading.setVisibility(View.VISIBLE);
            }
        });
        binding.close.setOnClickListener((v)->{
            binding.searchField.setText("");

        });
        loadMoreContent();
        onClickEnter();
    }

    @Override
    public void onDestroy() {
//        Glide.with(this).clearOnStop();
        binding.loading.setVisibility(View.GONE);
        if (timer != null && !isTimerCanceled) {
            timer.cancel();
            isTimerCanceled = true;
        }
        WorkRequest workRequest = new OneTimeWorkRequest.Builder(ClearLogosTemp.class).build();
        WorkManager.getInstance(getApplicationContext()).enqueue(workRequest);
        super.onDestroy();


    }

    @Override
    public void onResume() {
        super.onResume();
        this.controllClick = false;
        alredyClicked = false;
    }

    public void showSearch() {

        searchIsShowed = true;
        binding.headerContainer1.setVisibility(View.VISIBLE);
        ValueAnimator show = ValueAnimator.ofInt(-60, 0);
        show.setDuration(500);
        binding.searchItens.setVisibility(View.VISIBLE);

        timer = new Timer();
        isTimerCanceled = false;
        timer.schedule(new TimerTask() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void run() {

                if (search_has_change && !Objects.requireNonNull(binding.searchField.getText()).toString().isEmpty() && !isSearching && Instant.now().toEpochMilli() - last_time_key_pressed > 700) {
                    search_has_change = false;
                    isSearching = true;
                    data = aniListApiRequester.searchTitles(binding.searchField.getText().toString());
                    adapterSearchItensSimpleResult.setData(data);
                    ExtensionShowContentActivity.this.runOnUiThread(() -> {
                        adapterSearchItensSimpleResult.notifyDataSetChanged();
                        binding.searchItens.setVisibility(View.VISIBLE);
                        isSearching = false;
                        binding.loading.setVisibility(View.GONE);
                    });
                    isSearching = false;

                    return;
                }

            }
        }, 1500, 1700);

        show.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams) binding.headerContainer1.getLayoutParams();
                lp1.topMargin = (int) (((int) animation.getAnimatedValue()) * getResources().getDisplayMetrics().density);
                binding.headerContainer1.setLayoutParams(lp1);
            }
        });
        show.start();
    }

    public void hiddenSearch() {
        searchIsShowed = false;
        if (!isTimerCanceled) {
            timer.cancel();
            isTimerCanceled = true;
        }
        binding.loading.setVisibility(View.GONE);

        ValueAnimator hidden = ValueAnimator.ofInt(0, -60);
        hidden.setDuration(500);
        binding.searchItens.setVisibility(View.GONE);
        hidden.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams) binding.headerContainer1.getLayoutParams();
                lp1.topMargin = (int) (((int) animation.getAnimatedValue()) * getResources().getDisplayMetrics().density);
                binding.headerContainer1.setLayoutParams(lp1);
            }
        });
        hidden.start();

    }

    private void onClickEnter() {
        binding.searchField.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (!alredyClicked && keyCode == KeyEvent.KEYCODE_ENTER) {
                    alredyClicked = true;
                    Intent intent = new Intent(ExtensionShowContentActivity.this, SearchResultActivity.class);
                    intent.putExtra("language", language);
                    intent.putExtra("SearchField", binding.searchField.getText().toString());
                    intent.putExtra("Extension", mangaDexExtension instanceof MangaDexExtension ? Extensions.MANGADEX : Extensions.MANGAKAKALOT);
                    ExtensionShowContentActivity.this.startActivity(intent);
                }
                return true;
            }
        });

    }

    private void loadMoreContent() {
        binding.reciclerViewMangas.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && !supressManyLoad) {

                    supressManyLoad = true;
                    workerThread = new Thread() {
                        @Override
                        public void run() {
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

    private void startUphandler() {
        mainHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                //System.out.println("Dados: "+msg.getData().get("dados"));

                if (msg.what == Extensions.RESPONSE_ITEM) {
                    isAdvancedSearchSettingsChanged = false;
                    binding.progressTop.setVisibility(View.GONE);
                    binding.progressBotton.setVisibility(View.GONE);
                    mangasFromDataBaseViewModel.getMangas().add(msg.getData().getParcelable("dados"));

                    adapter.notifyItemInserted(mangasFromDataBaseViewModel.getMangas().size() - 1);

                    controllLoad++;
                    if (controllLoad == 27) {
                        supressManyLoad = false;
                        controllLoad = 0;
                    }

                } else if (msg.what == Extensions.RESPONSE_FINAL) {
                    binding.progressTop.setVisibility(View.GONE);
                    binding.progressBotton.setVisibility(View.GONE);
                    supressManyLoad = false;
                } else if (msg.what == Extensions.RESPONSE_ERROR) {
                    binding.progressTop.setVisibility(View.GONE);
                    binding.progressBotton.setVisibility(View.GONE);
                    supressManyLoad = false;
                }

            }
        };
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);

    }

    @Override
    public void onPause(){
        super.onPause();
        binding.loading.setVisibility(View.GONE);
        if (timer != null && !isTimerCanceled) {
            timer.cancel();
            isTimerCanceled = true;
        }
    }
}

