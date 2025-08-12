package com.example.trinity.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.transition.TransitionInflater;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.trinity.Interfaces.Extensions;
import com.example.trinity.MainActivity;


import com.example.trinity.MangaShowContentActivity;
import com.example.trinity.R;
import com.example.trinity.adapters.AdapterPages;

import com.example.trinity.adapters.AdapterPagesCascade;
import com.example.trinity.databinding.FragmentReaderMangaBinding;
import com.example.trinity.databinding.ReaderAlphaDialogLayoutBinding;
import com.example.trinity.databinding.ReaderConfigDialogLoyoutBinding;
import com.example.trinity.extensions.MangaDexExtension;
import com.example.trinity.extensions.MangakakalotExtension;
import com.example.trinity.models.Model;
import com.example.trinity.preferecesConfig.ConfigClass;
import com.example.trinity.services.ClearPageCacheWork;
import com.example.trinity.storageAcess.ChapterStorageManager;
import com.example.trinity.valueObject.ChapterManga;

import com.example.trinity.valueObject.ChapterUpdated;
import com.example.trinity.valueObject.History;
import com.example.trinity.viewModel.MangaDataViewModel;
import com.example.trinity.viewModel.UpdatesViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReaderMangaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReaderMangaFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private FragmentReaderMangaBinding binding;
    private AdapterPages adapterPages;
    private String[] imageURI;
    private boolean isShowedTopAndBotton = false;
    private Handler mainHandler;
    private Thread workThread;

    private int currentDecoration;
    private int newDecoration;
    private View decoration;
    private Extensions mangaDexExtension;
    private GestureDetector gestureDetector;
    private ValueAnimator vm;
    private int chapterIndex;
    private String idChap;
    private ArrayList<ChapterManga> chapters;
    private boolean radioButtomChangeFromUser = true;

    private boolean loadingFragmentContent = true;

    private boolean isLoadingNewChapter = false;
    private SharedPreferences preferences;
    private SharedPreferences.Editor preferencesEditor;
    private int alpha;
    private int readDirection = 1;
    private Model model;
    private MangaDataViewModel mangaDataViewModel;
    private long initialTime, endTime;
    private int startPage = 0;
    private boolean isDownloading = false;
    private String mangaId = "", mangaLanguage = "";
    private AdapterPagesCascade adapterPagesCascade;
    private boolean chapterDownloaded = false;
    private ChapterStorageManager storageManager;
    private WindowInsetsControllerCompat windowInsetsControllerCompat;
    private boolean isMangaAdded;
    private UpdatesViewModel updatesViewModel;
    private ValueAnimator animatorLeft;
    private LinearLayout.LayoutParams layoutParamsLeft;
    private LinearLayout.LayoutParams layoutParamsRight;
    private ValueAnimator animatorRight;

    private AlertDialog.Builder configDialog;
    private ReaderConfigDialogLoyoutBinding configDialogLoyoutBinding;

    private AlertDialog.Builder alphaDialog;
    private ReaderAlphaDialogLayoutBinding alphaDialogLayoutBinding;

    public ReaderMangaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReaderMangaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReaderMangaFragment newInstance(String param1, String param2) {
        ReaderMangaFragment fragment = new ReaderMangaFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
        TransitionInflater inflater = TransitionInflater.from(getActivity());
        setExitTransition(inflater.inflateTransition(R.transition.fragment_transition));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReaderMangaBinding.inflate(inflater, container, false);

        storageManager = new ChapterStorageManager(this.getContext());
//        windowInsetsCompat = WindowCompat.getInsetsController(getActivity().getWindow(), getActivity().getWindow().getDecorView());
//        windowInsetsCompat.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsControllerCompat = WindowCompat.getInsetsController(getActivity().getWindow(), getActivity().getWindow().getDecorView());
        windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.statusBars());
        if (getArguments() != null) {
            try {
                this.startPage = getArguments().getInt("currentPage");
            } catch (NullPointerException ex) {
                this.startPage = 0;
            }
            mangaId = getArguments().getString("mangaIdApi", "invalid");
            mangaLanguage = getArguments().getString("mangaLanguage", "invalid");
            isMangaAdded = getArguments().getBoolean("isMangaAdded");
        }


        if (this.getActivity() instanceof MainActivity) {
            this.updatesViewModel = new ViewModelProvider(requireActivity()).get(UpdatesViewModel.class);
        }

        View v = binding.getRoot();
        v.setFitsSystemWindows(true);
        mangaDataViewModel = new ViewModelProvider(getActivity()).get(MangaDataViewModel.class);
        preferences = getActivity().getSharedPreferences(ConfigClass.TAG_PREFERENCE, MODE_PRIVATE);
        preferencesEditor = preferences.edit();
        String imageQuality = preferences.getString(ConfigClass.ConfigContent.IMAGE_QUALITY, "dataSaver");

        this.alpha = preferences.getInt(ConfigClass.ConfigReader.ALPHA_CONFIG, 100);
//        this.binding.alphaController.setProgress(this.alpha);


        this.readDirection = preferences.getInt(ConfigClass.ConfigReader.READ_DIRECTION, 1);

        if (readDirection == 1) {
            binding.cascadeRead.setVisibility(View.GONE);
            binding.pageContainer.setVisibility(View.VISIBLE);
            binding.pageContainer.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        } else if (readDirection == 2) {
            binding.cascadeRead.setVisibility(View.GONE);
            binding.pageContainer.setVisibility(View.VISIBLE);
            binding.pageContainer.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
//            configDialogLoyoutBinding.cascade.setChecked(true);
            binding.cascadeRead.setVisibility(View.VISIBLE);
            binding.pageContainer.setVisibility(View.GONE);
            binding.seekBar.setVisibility(View.GONE);
        }

        binding.alpha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alphaDialog = new AlertDialog.Builder(requireContext(), R.style.RoundAlertDialog);
                alphaDialogLayoutBinding = ReaderAlphaDialogLayoutBinding.inflate(LayoutInflater.from(requireContext()));
                alphaDialogLayoutBinding.alphaController.setProgress(alpha);
                alphaControllerSetup();
                alphaDialog.setView(alphaDialogLayoutBinding.getRoot());
                alphaDialog.setTitle("Configurar a luminosidade da leitura");

                AlertDialog alertDialog = alphaDialog.create();
                Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_shape);
                alertDialog.show();

            }
        });
        binding.alpha.bringToFront();

        decoration = getActivity().getWindow().getDecorView();
        this.currentDecoration = decoration.getSystemUiVisibility();
        this.newDecoration = decoration.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decoration.setSystemUiVisibility(newDecoration);
//        decoration.setFitsSystemWindows(false);
        vm = ValueAnimator.ofFloat(0, 360);
        vm.setDuration(1000);
        vm.setRepeatCount(ValueAnimator.INFINITE);
        vm.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                binding.errorOnLoad.setRotation((float) animation.getAnimatedValue());
            }
        });


        binding.getRoot().bringChildToFront(binding.Header);
        binding.getRoot().bringChildToFront(binding.footer);

        idChap = mangaDataViewModel.getIdChap();
        String mangaName = mangaDataViewModel.getManga().getTitulo();
        chapters = mangaDataViewModel.getManga().getChapters();

        chapters.sort(Comparator.comparingDouble(c -> Double.parseDouble(c.getChapter())));
//        try {
//            SortUtilities.dinamicSort("com.example.trinity.valueObject.ChapterManga", "getChapter", chapters, OrderEnum.CRESCENTE);
//        } catch (NoSuchMethodException e) {
//            throw new RuntimeException(e);
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        } catch (IllegalAccessException e) {
//            throw new RuntimeException(e);
//        } catch (InvocationTargetException e) {
//            throw new RuntimeException(e);
//        }

        chapterIndex = this.getChapterIndex(idChap);
        new Thread() {
            @Override
            public void run() {
                model = Model.getInstance(getActivity());
                model.setLastChapterRead(chapters.get(chapterIndex).getId(), mangaId, mangaLanguage);
            }
        }.start();
        binding.chapTitle.setText(this.chapters.get(chapterIndex).getChapter() + " - " + this.chapters.get(chapterIndex).getTitle());


        binding.pageContainer.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                adapterPages.currentItem = position;
                binding.seekBar.setProgress(position + 1);

                if (position == imageURI.length - 1 - adapterPages.getAmountPagesIgnored()) {
                    Instant now = Instant.now();
                    endTime = now.getEpochSecond();
                    adapterPages.setTimeWasteOnRead(endTime - initialTime);
                    adapterPages.notifyItemChanged(imageURI.length - 1 - adapterPages.getAmountPagesIgnored());

                    binding.seekBar.setVisibility(View.GONE);

                } else if (position == 0) {
                    binding.seekBar.setVisibility(View.GONE);

                } else {
                    binding.seekBar.setVisibility(View.VISIBLE);

                }

                if (chapterDownloaded && position > 0 && position < imageURI.length - 1 && imageURI[position] == null) {

                    new Thread() {
                        @Override
                        public void run() {
                            mangaDexExtension.loadUniquePage(chapters.get(chapterIndex).getId(), position - 1, mainHandler);
                        }
                    }.start();
                }
            }
        });
//        binding.pageContainer.setUserInputEnabled(false);


        mainHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == Extensions.RESPONSE_ITEM) {

                    Glide.get(requireActivity()).clearMemory();
                    new Thread() {
                        @Override
                        public void run() {
                            Glide.get(requireActivity()).clearDiskCache();
                            Instant i = Instant.now();
                            model = Model.getInstance(getActivity());
                            model.addOrUpdateReadingHitory(new History(mangaDataViewModel.getManga(), i.getEpochSecond()));
                        }
                    }.start();

                    isLoadingNewChapter = false;
                    Instant now = Instant.now();
                    initialTime = now.getEpochSecond();
//                    controllShowBottomTopBar();
                    int numPages = msg.getData().getInt("numPages");

                    imageURI = new String[numPages + 2];
                    adapterPages = new AdapterPages(getActivity(), imageURI);
                    adapterPages.setLogoManga(mangaDataViewModel.getManga().getId());

//                    adapterPages.setFavorited(isMangaAdded);
                    adapterPages.setAlpha(alpha);


                    adapterPagesCascade = new AdapterPagesCascade(getActivity(), imageURI, ReaderMangaFragment.this);
//                    adapterPagesCascade.setLogoManga(mangaDataViewModel.getManga().getImage());
//                    binding.cascadeRead.setAlpha((float) alpha / 100);
                    adapterPagesCascade.setLogoManga(mangaDataViewModel.getManga().getId());
                    adapterPages.setFragment(ReaderMangaFragment.this);
                    adapterPages.setLogoManga(mangaDataViewModel.getManga().getId());
                    if (chapterIndex == chapters.size() - 1) {
                        adapterPages.setIslastChapter(true);
                        adapterPagesCascade.setLastChapter(true);
                        binding.nextChap.setVisibility(View.GONE);
                    }
                    if (chapterIndex == 0) {
                        adapterPages.setIsFirstChapter(true);
                        adapterPagesCascade.setFirstChapter(true);
                        binding.prevChap.setVisibility(View.GONE);
                    }
                    if (chapterIndex > 0 && chapterIndex < chapters.size() - 1) {
                        binding.nextChap.setVisibility(View.VISIBLE);
                        binding.nextChap.setVisibility(View.VISIBLE);
                    }
                    binding.seekBar.setMax(imageURI.length-adapterPages.getAmountPagesIgnored());
                    binding.seekBar.setMin(1);

                    binding.pageContainer.setAdapter(adapterPages);

                    binding.cascadeRead.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                    binding.cascadeRead.setAdapter(adapterPagesCascade);
                    binding.cascadeRead.setHasFixedSize(true);

                    Timer t = new Timer();
                    t.schedule(new TimerTask() {
                        @Override
                        public void run() {

                            ReaderMangaFragment.this.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    binding.pageContainer.setCurrentItem(startPage != 0 ? startPage : 0);
                                    binding.cascadeRead.scrollToPosition(startPage != 0 ? startPage : 0);
                                    binding.seekBar.setProgress(startPage == 0 ? 1 : startPage + 1, true);
                                    if (startPage != 0) {
                                        binding.numPages.setText((startPage + 1) + " / " + imageURI.length);
                                    }
                                    startPage = 0;
                                }
                            });
                        }
                    }, 500);

                    adapterPages.notifyItemChanged(startPage);
                    adapterPages.setReverseStartReadLogo(readDirection == 2);

                    binding.pageContainer.setLayoutDirection(readDirection == 1 ? View.LAYOUT_DIRECTION_LTR : View.LAYOUT_DIRECTION_RTL);
                    binding.numPages.bringToFront();
                    binding.errorContainer.setVisibility(View.GONE);
                    binding.numPages.setText("1 / " + (imageURI.length - adapterPages.getAmountPagesIgnored()));


                    binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (binding.pageContainer.isUserInputEnabled()) {
                                binding.numPages.setText(progress + " / " + (imageURI.length - adapterPages.getAmountPagesIgnored()));

                                if (fromUser) {
                                    binding.cascadeRead.scrollToPosition(progress - 1);
                                    binding.pageContainer.setCurrentItem(progress - 1, true);
                                }
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                            seekBar.setAlpha(1f);
                            binding.nextChap.setAlpha(1f);
                            binding.prevChap.setAlpha(1f);
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            ValueAnimator valueAnimator = ValueAnimator.ofFloat(1f, 0.1f);
                            valueAnimator.setDuration(500);

                            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                                    seekBar.setAlpha((float) animation.getAnimatedValue());
                                    binding.nextChap.setAlpha((float) animation.getAnimatedValue());
                                    binding.prevChap.setAlpha((float) animation.getAnimatedValue());
                                }
                            });
                            valueAnimator.start();
                        }
                    });

                } else if (msg.what == Extensions.RESPONSE_PAGE) {
                    vm.cancel();
                    binding.animationContainer.setVisibility(View.GONE);
                    if (animatorLeft != null) {
                        if (animatorLeft.isRunning()) animatorLeft.cancel();
                    }
                    if (animatorRight != null) {
                        if (animatorRight.isRunning()) animatorRight.cancel();
                    }
                    binding.errorContainer.setVisibility(View.GONE);
                    String bit = msg.getData().getString("img");

                    imageURI[msg.getData().getInt("index")] = bit;

                    adapterPagesCascade.notifyItemChanged(msg.getData().getInt("index"));

                    adapterPages.notifyItemChanged(msg.getData().getInt("index"));
                    binding.seekBar.bringToFront();

                } else if (msg.what == Extensions.RESPONSE_ERROR) {
                    binding.errorContainer.setVisibility(View.VISIBLE);
                } else if (msg.what == Extensions.RESPONSE_COUNT_ITENS_DECREASED_BY_ONE) {
                    adapterPages.ignorePage();
                    adapterPagesCascade.ignorePage();
                    binding.numPages.setText(String.format("1 / %d", 0));
                    binding.seekBar.setMax(imageURI.length - adapterPages.getAmountPagesIgnored());
                }

            }
        };

        binding.goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getActivity() instanceof MangaShowContentActivity) {
                    MangaShowContentActivity mangaShowContentActivity = (MangaShowContentActivity) getActivity();
                    mangaShowContentActivity.backToInfoManga();
                    return;
                }
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.backToUpdates();


            }
        });
        mangaDexExtension = mangaDataViewModel.getManga().getId().contains(MangakakalotExtension.MANGAKAKALOT) || mangaDataViewModel.getManga().getId().contains(MangakakalotExtension.CHAPMANGANATO) ? new MangakakalotExtension(null) : new MangaDexExtension(mangaLanguage, imageQuality);
        mangaDexExtension.setContext(getActivity());
        workThread = new Thread() {
            @Override
            public void run() {
//                System.out.println("worker Thread");
                if (chapters.get(chapterIndex).isDownloaded()) {
                    chapterDownloaded = true;
                    storageManager.getChapterPages(chapters.get(chapterIndex).getId(), mainHandler);
                    return;
                }
                mangaDexExtension.getChapterPages(mainHandler, chapters.get(chapterIndex).getId());
            }
        };
        workThread.start();

        tryReload();
        configShowUp();

        startUpPageButtons();
        downloadStartUp();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {

            @Override
            public void handleOnBackPressed() {
                if (getActivity() instanceof MangaShowContentActivity) {
                    MangaShowContentActivity mangaShowContentActivity = (MangaShowContentActivity) getActivity();
                    mangaShowContentActivity.backToInfoManga();
                    return;
                }
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.backToUpdates();
            }
        };
        getActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);
        binding.Header.bringToFront();
        binding.footer.bringToFront();

        return v;
    }

    private void configShowUp() {
        binding.settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                configDialogLoyoutBinding = ReaderConfigDialogLoyoutBinding.inflate(LayoutInflater.from(requireContext()));
                if (readDirection == 1) {
                    configDialogLoyoutBinding.leftToRight.setChecked(true);
                    binding.seekBar.setVisibility(View.VISIBLE);
                } else if (readDirection == 2) {
                    configDialogLoyoutBinding.rightToLeft.setChecked(true);
                    binding.seekBar.setVisibility(View.VISIBLE);
                } else {
                    configDialogLoyoutBinding.cascade.setChecked(true);
                    binding.cascadeRead.setVisibility(View.VISIBLE);
                    binding.pageContainer.setVisibility(View.GONE);
                    binding.seekBar.setVisibility(View.GONE);
                }
                startUpRadioGroup();
//                if(configDialog == null){
                configDialog = new AlertDialog.Builder(requireActivity(), R.style.RoundAlertDialog);
//                    configDialogLoyoutBinding.leftToRight.setChecked(true);
                configDialog.setView(configDialogLoyoutBinding.getRoot());

                configDialog.setTitle("Definir a horientação de leitura");


//                }
                AlertDialog alertDialog = configDialog.create();
                Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_shape);
                alertDialog.show();
            }
        });
    }

    public void tryReload() {
        binding.errorOnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vm.start();
                workThread = new Thread() {
                    @Override
                    public void run() {
                        mangaDexExtension.getChapterPages(mainHandler, chapters.get(chapterIndex).getId());


                    }

                };
                workThread.start();

            }
        });

//        this.alphaControllerSetup();
//        this.binding.readConfigsContainer.bringToFront();
    }


    @Override
    public void onDestroy() {
        WorkRequest workRequest = new OneTimeWorkRequest.Builder(ClearPageCacheWork.class).build();
        WorkManager.getInstance(requireActivity().getApplicationContext()).enqueue(workRequest);
        Context appContext = requireContext().getApplicationContext();
        Glide.get(appContext).clearMemory();
        new Thread() {
            @Override
            public void run() {
                Glide.get(appContext).clearDiskCache();
            }
        }.start();
        if (animatorLeft != null) {
            if (animatorLeft.isRunning()) animatorLeft.cancel();
        }
        if (animatorRight != null) {
            if (animatorRight.isRunning()) animatorRight.cancel();
        }

        super.onDestroy();

        if (workThread != null && workThread.isAlive()) {
            workThread.interrupt();
        }
//        mangaDataViewModel.setIdChap("");
        mangaDataViewModel = null;


    }


    public void controllUserInput(boolean isPaginable) {
        binding.pageContainer.setUserInputEnabled(isPaginable);
//        System.out.println(pageContainer.isUserInputEnabled());
    }

    public void controllShowBottomTopBar() {
        if (this.getActivity() == null) {
            return;
        }


//        binding.alphaControlContainer.setVisibility(View.GONE);
//        binding.readConfigsContainer.setVisibility(View.GONE);
        ValueAnimator animatorShow = ValueAnimator.ofInt(-50, 0);
        animatorShow.setRepeatCount(0);
        animatorShow.setDuration(300);
        animatorShow.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                int margins = (int) animation.getAnimatedValue();
                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        (int) (50 * getResources().getDisplayMetrics().density)
                );

                ConstraintLayout.LayoutParams layoutParams2 = new ConstraintLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        (int) (50 * getResources().getDisplayMetrics().density)
                );

                layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                layoutParams.topMargin = (int) (margins * getResources().getDisplayMetrics().density);


                binding.Header.setLayoutParams(layoutParams);

                layoutParams2.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                layoutParams2.bottomMargin = (int) (margins * getResources().getDisplayMetrics().density);

                binding.footer.setLayoutParams(layoutParams2);
            }
        });
        ValueAnimator animatorHidden = ValueAnimator.ofInt(0, -50);
        animatorHidden.setDuration(300);
        animatorHidden.setRepeatCount(0);

        animatorHidden.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                int margins = (int) animation.getAnimatedValue();
                if (!isAdded() || getContext() == null) return;
                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        (int) (50 * getResources().getDisplayMetrics().density)
                );
                ConstraintLayout.LayoutParams layoutParams2 = new ConstraintLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        (int) (50 * getResources().getDisplayMetrics().density));

                layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                layoutParams.topMargin = (int) (margins * getResources().getDisplayMetrics().density);

                binding.Header.setLayoutParams(layoutParams);
                layoutParams2.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                layoutParams2.bottomMargin = (int) (margins * getResources().getDisplayMetrics().density);

                binding.footer.setLayoutParams(layoutParams2);
            }
        });
        if (!isShowedTopAndBotton) {
//            decoration.setSystemUiVisibility(currentDecoration);
            animatorShow.start();
            binding.seekBar.setAlpha(1f);
            binding.nextChap.setAlpha(1f);
            binding.prevChap.setAlpha(1f);
            windowInsetsControllerCompat.show(WindowInsetsCompat.Type.statusBars());
            windowInsetsControllerCompat.show(WindowInsetsCompat.Type.navigationBars());
            isShowedTopAndBotton = true;
            return;
        }
        decoration.setSystemUiVisibility(newDecoration);
        windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.statusBars());

        animatorHidden.start();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1f, 0.1f);
        valueAnimator.setDuration(500);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                binding.seekBar.setAlpha((float) animation.getAnimatedValue());
                binding.nextChap.setAlpha((float) animation.getAnimatedValue());
                binding.prevChap.setAlpha((float) animation.getAnimatedValue());
            }
        });
        valueAnimator.start();
        isShowedTopAndBotton = false;
    }

    private int getChapterIndex(String id) {

        for (int i = 0; i < this.chapters.size(); i++) {

            if (this.chapters.get(i).getId().equals(id)) {
                chapterDownloaded = this.chapters.get(i).isDownloaded();
                return i;
            }
        }
        return -1;
    }

    public void nextChapter() {
        preferencesEditor.putInt(ConfigClass.ConfigReader.ALPHA_CONFIG, alpha);
        preferencesEditor.apply();

        if (!isLoadingNewChapter && chapterIndex < chapters.size()) {
            if (mangaDataViewModel != null && mangaDataViewModel.getManga() != null && mangaDataViewModel.getManga().getChapters() != null) {
                if (!mangaDataViewModel.getManga().getChapters().isEmpty()) {
                    mangaDataViewModel.getManga().getChapters().get(chapterIndex).setAlredyRead(true);
                }
            }
            newChapterAnimationStartUp();
            new Thread() {
                @Override
                public void run() {
                    Model model = Model.getInstance(getActivity());
                    model.chapterRead(chapters.get(chapterIndex));
                    model.setLastChapterRead(chapters.get(chapterIndex).getId(), mangaId, mangaLanguage);

                    if (updatesViewModel != null) {
                        for (ChapterUpdated updated : Objects.requireNonNull(updatesViewModel.getChapterUpdatedLiveData().getValue())) {
                            if (updated != null && updated.getChapterManga().getId().equals(chapters.get(chapterIndex).getId())) {
                                updated.getChapterManga().setAlredyRead(true);
                            }
                        }
                    }
                }
            }.start();
            this.chapterIndex++;
            binding.chapTitle.setText(this.chapters.get(chapterIndex).getChapter() + " - " + this.chapters.get(chapterIndex).getTitle());
            isLoadingNewChapter = true;
            if (workThread.isAlive()) {
                workThread.interrupt();
            }
            workThread = new Thread() {
                @Override
                public void run() {
                    if (chapters.get(chapterIndex).isDownloaded()) {
                        chapterDownloaded = true;
                        storageManager.getChapterPages(chapters.get(chapterIndex).getId(), mainHandler);
                        return;
                    }
                    mangaDexExtension.getChapterPages(mainHandler, chapters.get(chapterIndex).getId());
                }
            };
            workThread.start();
        }
    }

    public void previousChapter() {
        preferencesEditor.putInt(ConfigClass.ConfigReader.ALPHA_CONFIG, alpha);
        preferencesEditor.apply();
        if (!isLoadingNewChapter && this.chapterIndex > 0) {
            if (mangaDataViewModel != null && mangaDataViewModel.getManga() != null && mangaDataViewModel.getManga().getChapters() != null) {
                if (!mangaDataViewModel.getManga().getChapters().isEmpty()) {
                    mangaDataViewModel.getManga().getChapters().get(chapterIndex).setAlredyRead(true);
                }
            }
            newChapterAnimationStartUp();
            new Thread() {
                @Override
                public void run() {
                    Model model = Model.getInstance(getActivity());
                    model.chapterRead(chapters.get(chapterIndex));
                    model.setLastChapterRead(chapters.get(chapterIndex).getId(), mangaId, mangaLanguage);
                }
            }.start();

            this.chapterIndex--;
            binding.chapTitle.setText(this.chapters.get(chapterIndex).getChapter() + " - " + this.chapters.get(chapterIndex).getTitle());
            isLoadingNewChapter = true;
            workThread.interrupt();
            workThread = new Thread() {
                @Override
                public void run() {
                    if (chapters.get(chapterIndex).isDownloaded()) {
                        chapterDownloaded = true;
                        storageManager.getChapterPages(chapters.get(chapterIndex).getId(), mainHandler);
                        return;
                    }
                    mangaDexExtension.getChapterPages(mainHandler, chapters.get(chapterIndex).getId());
                }
            };
            workThread.start();

        }
    }

    private void alphaControllerSetup() {
        alphaDialogLayoutBinding.alphaController.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                adapterPages.setAlpha(progress);
                binding.cascadeRead.setAlpha((float) progress / 100);
                binding.pageContainer.setAlpha((float) progress / 100);
                alpha = progress;

//                seekBar.setThumb(getActivity().getDrawable(R.drawable.light_controll_active));

//                adapterPages.notifyDataSetChanged();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Drawable drawable = AppCompatResources.getDrawable(requireContext(), R.drawable.light_controll_active);
                TypedValue typedValue = new TypedValue();
                requireActivity().getTheme().resolveAttribute(com.google.android.material.R.attr.colorTertiary, typedValue, true);
                assert drawable != null;
                drawable.setTint(typedValue.data);
                seekBar.setThumb(drawable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Drawable drawable = AppCompatResources.getDrawable(requireContext(), R.drawable.light_controll_active);
                TypedValue typedValue = new TypedValue();
                requireActivity().getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
                assert drawable != null;
                drawable.setTint(typedValue.data);
                seekBar.setThumb(drawable);
            }
        });

    }

    private void startUpRadioGroup() {

        configDialogLoyoutBinding.radio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (configDialogLoyoutBinding.leftToRight.isChecked()) {
                    binding.pageContainer.setVisibility(View.VISIBLE);
                    binding.cascadeRead.setVisibility(View.GONE);
                    binding.pageContainer.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                    adapterPages.setReverseStartReadLogo(false);
                    adapterPages.notifyDataSetChanged();
                    preferencesEditor.putInt(ConfigClass.ConfigReader.READ_DIRECTION, 1);
                    readDirection = 1;
                    preferencesEditor.apply();
//                    radioButtomChangeFromUser = true;
                    return;
                }
                if (configDialogLoyoutBinding.cascade.isChecked()) {
                    binding.pageContainer.setVisibility(View.GONE);
                    binding.cascadeRead.setVisibility(View.VISIBLE);
//                    if(radioButtomChangeFromUser){
                    preferencesEditor.putInt(ConfigClass.ConfigReader.READ_DIRECTION, 3);
                    preferencesEditor.apply();
//                    }
                    readDirection = 3;
//                    radioButtomChangeFromUser = true;
                    return;
                }
                binding.pageContainer.setVisibility(View.VISIBLE);
                binding.cascadeRead.setVisibility(View.GONE);
                binding.pageContainer.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                preferencesEditor.putInt(ConfigClass.ConfigReader.READ_DIRECTION, 2);
                adapterPages.setReverseStartReadLogo(true);
                adapterPages.notifyDataSetChanged();
                readDirection = 2;
//                radioButtomChangeFromUser = true;
                preferencesEditor.apply();
            }
        });
//        if(!preferences.getBoolean(ConfigClass.ConfigReader.ALWAYS_CASCADE_WHEN_LONG_STRIP,false))return;
//        for(TagManga t:mangaDataViewModel.getManga().getTags()){
//            if(t.getNome().equals("Long Strip")){
//                radioButtomChangeFromUser = false;
//                binding.cascade.setChecked(true);
//            }
//        }

    }

    public void setPageCascade(int item) {
        binding.seekBar.setProgress(item, true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.controllShowBottomNavigator(this);
            mainActivity.isInFirstDestination = false;
            mainActivity.isInReadFragment = true;
        }
    }

    @Override
    public void onPause() {
//        System.out.println("pausado");
        preferencesEditor.putInt(ConfigClass.ConfigReader.ALPHA_CONFIG, alpha);
        preferencesEditor.apply();
        decoration.setSystemUiVisibility(currentDecoration);
        windowInsetsControllerCompat.show(WindowInsetsCompat.Type.statusBars());
        super.onPause();
        new Thread() {
            @Override
            public void run() {
                model = Model.getInstance(getActivity());
                model.setChapterLastPage(chapters.get(chapterIndex).getId(), binding.pageContainer.getCurrentItem());
            }
        }.start();

    }

    public void nextPage() {
        binding.pageContainer.setCurrentItem(1, true);
    }

    public void nextPageCascade() {

        binding.cascadeRead.scrollToPosition(2);

    }

    private void startUpPageButtons() {
        binding.nextChap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextChapter();
            }
        });
        binding.prevChap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousChapter();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")

    private void downloadStartUp() {
        binding.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDownloading) return;
                isDownloading = true;
                new Thread() {
                    public void run() {
                        download();
                    }
                }.start();
            }
        });


    }

    public void download() {

        String imageName = mangaDataViewModel.getManga().getTitulo() + "_" + Instant.now().getEpochSecond() + ".jpeg";
        String url = imageURI[binding.pageContainer.getCurrentItem()];
        if (url == null) {
            return;
        }
        Bitmap image = null;
        try (FileInputStream fileInputStream = new FileInputStream(new File(url))) {

            image = BitmapFactory.decodeStream(fileInputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        if (image == null) {
            isDownloading = false;
            Toast.makeText(getActivity().getApplicationContext(), "Não foi possível salvar a imagem", Toast.LENGTH_LONG).show();
            return;
        }

        ContentValues imageValue = new ContentValues();
        imageValue.put(MediaStore.MediaColumns.DISPLAY_NAME, imageName);
        imageValue.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        imageValue.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        ContentResolver contentResolver = getActivity().getContentResolver();

        Uri uri = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, imageValue);
        }

        if (uri == null) return;

        try (OutputStream outputStream = contentResolver.openOutputStream(uri)) {

            if (outputStream == null) return;

            image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();

        } catch (FileNotFoundException e) {
            isDownloading = false;
            throw new RuntimeException(e);

        } catch (IOException e) {
            isDownloading = false;
            throw new RuntimeException(e);

        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity().getApplicationContext(), "Imagem salva na pasta de downloads📖", Toast.LENGTH_LONG).show();
            }
        });
        isDownloading = false;
    }

    private void newChapterAnimationStartUp() {
//        if(!isLoadingNewChapter)return;
        layoutParamsLeft = (LinearLayout.LayoutParams) binding.left.getLayoutParams();
        layoutParamsRight = (LinearLayout.LayoutParams) binding.right.getLayoutParams();

        binding.animationContainer.setVisibility(View.VISIBLE);

        animatorLeft = ValueAnimator.ofInt(30, 10);
        animatorRight = ValueAnimator.ofInt(10, 30);
        animatorLeft.setRepeatCount(ValueAnimator.INFINITE);
        animatorRight.setRepeatCount(ValueAnimator.INFINITE);
        animatorLeft.setRepeatMode(ValueAnimator.REVERSE);
        animatorRight.setRepeatMode(ValueAnimator.REVERSE);
        animatorLeft.setDuration(300);
        animatorRight.setDuration(300);

        animatorLeft.addUpdateListener(animation -> {
            layoutParamsLeft.width = (int) animation.getAnimatedValue() * (int) requireActivity().getResources().getDisplayMetrics().density;
            layoutParamsLeft.height = (int) animation.getAnimatedValue() * (int) requireActivity().getResources().getDisplayMetrics().density;
            binding.left.setLayoutParams(layoutParamsLeft);
        });

        animatorRight.addUpdateListener(animation -> {
            layoutParamsRight.width = (int) animation.getAnimatedValue() * (int) requireActivity().getResources().getDisplayMetrics().density;
            layoutParamsRight.height = (int) animation.getAnimatedValue() * (int) requireActivity().getResources().getDisplayMetrics().density;
            binding.right.setLayoutParams(layoutParamsRight);
        });

        animatorLeft.start();
        animatorRight.start();

    }

    @Override
    public void onStart() {
        super.onStart();
        if (loadingFragmentContent) {
            newChapterAnimationStartUp();
            loadingFragmentContent = false;
        }
    }
}