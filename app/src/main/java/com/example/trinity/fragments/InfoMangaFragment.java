package com.example.trinity.fragments;

import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.StateSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.trinity.Interfaces.Extensions;
import com.example.trinity.MangaShowContentActivity;
import com.example.trinity.R;
import com.example.trinity.adapters.AdapterChapters;
import com.example.trinity.adapters.AdapterGenres;
import com.example.trinity.databinding.FragmentInfoMangaBinding;
import com.example.trinity.extensions.MangaDexExtension;
import com.example.trinity.extensions.MangakakalotExtension;
import com.example.trinity.models.Model;
import com.example.trinity.preferecesConfig.ConfigClass;
import com.example.trinity.services.DownloadChapterWork;
import com.example.trinity.services.broadcasts.ActionsPending;
import com.example.trinity.storageAcess.LogoMangaStorage;
import com.example.trinity.storageAcess.LogoMangaStorageTemp;
import com.example.trinity.valueObject.ChapterManga;
import com.example.trinity.valueObject.History;
import com.example.trinity.valueObject.Manga;

import com.example.trinity.viewModel.MangaDataViewModel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InfoMangaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InfoMangaFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentInfoMangaBinding binding;
    private boolean isTextExpanded = false;
    private boolean isdescendingOrder = false;
    private boolean chaptersAvaible = false;
    private boolean isAdded = false;
    private String language;
    private boolean unlockAddManga = true;
    private boolean isFirstAnimation = true;
    private Model model;
    private Thread workerThread = null;
    private Manga manga;
    private ArrayList<ChapterManga> chapterMangasListed;
    private AdapterChapters chaptersAdapter;
    private Handler mainHandler;
    private Extensions mangaDexExtension;
    private MangaDataViewModel mangaDataViewModel;
    private View v;
    private List<ChapterManga>[] sublListsChapters;
    private int indexSubLists = 0;
    private boolean canLoadMoreContent = true;
    private ArrayList<ChapterManga> allChapters = new ArrayList<>();
    private String lastChapter = "";
    private WorkManager workManager;
    private LogoMangaStorageTemp storageTemp;
    private LogoMangaStorage storage;
    Animatable2.AnimationCallback animationCallback;
    private boolean canScroll = false;
    private boolean isOrdenedReverse = false;
    private boolean isFilterChaptersOn = false;
    public InfoMangaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InfoMangaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InfoMangaFragment newInstance(String param1, String param2) {
        InfoMangaFragment fragment = new InfoMangaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        chapterMangasListed = new ArrayList<>();
        workManager = WorkManager.getInstance(requireActivity().getApplicationContext());
        storage = new LogoMangaStorage(getActivity());
        storageTemp = new LogoMangaStorageTemp(getActivity());
        binding = FragmentInfoMangaBinding.inflate(inflater, container, false);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ConfigClass.TAG_PREFERENCE, Context.MODE_PRIVATE);
        String imageQuality = sharedPreferences.getString(ConfigClass.ConfigContent.IMAGE_QUALITY, "dataSaver");
        v = binding.getRoot();

        mangaDataViewModel = new ViewModelProvider(getActivity()).get(MangaDataViewModel.class);
        manga = mangaDataViewModel.getManga();
        mainHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if(msg.what == Extensions.RESPONSE_REQUEST_BYPASS){
//                    DdosBypass.bypass(requireContext(),mainHandler,msg.getData().getString("url"));
                }

                else if (msg.what == Extensions.RESPONSE_REQUEST_NEW_CONTENT_CALL) {
                    new Thread() {
                        @Override
                        public void run() {
                            allChapters = mangaDataViewModel.getManga().getChapters() != null && !mangaDataViewModel.getManga().getChapters().isEmpty() ? mangaDataViewModel.getManga().getChapters() : (mangaDexExtension instanceof MangaDexExtension?mangaDexExtension.viewChapters(manga.getId()):mangaDexExtension.viewChapters(manga.getId(),mainHandler));
                            mangaDataViewModel.getManga().setChapters(allChapters);
                            allChapters.sort(Comparator.comparingDouble((ChapterManga ch)->Double.parseDouble(ch.getChapter())).reversed());

                            chapterMangasListed.clear();
                            indexSubLists = 0;

                            if (getActivity() == null) {
                                return;
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!manga.isOngoing(allChapters)) binding.status.setText("Concluído");
                                    binding.numChapters.setText(allChapters.size() + " Capítulos");
                                    binding.sortChapterIcon.setVisibility(View.VISIBLE);
                                    binding.progressChapter.setVisibility(View.GONE);
                                    mangaDataViewModel.getManga().setChapters(allChapters);
                                    chaptersAdapter = new AdapterChapters(requireActivity(), allChapters);
                                    chaptersAdapter.setMangaDataViewModel(mangaDataViewModel);
                                    chaptersAdapter.setFragment(InfoMangaFragment.this);
                                    binding.chapterContainer.setAdapter(chaptersAdapter);

                                }
                            });


                        }
                    }.start();
                }
            }
        };
        binding.title.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager manager = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setPrimaryClip(ClipData.newPlainText("Título do mangá", manga.getTitulo()));
                Toast.makeText(requireContext(), "Título copiado para a área de transferência", Toast.LENGTH_LONG).show();
                return true;
            }
        });

        isAdded = manga.isAdded;
        if (manga.isAdded) {
//            binding.readState.setImageResource(R.drawable.adicionado_a_biblioteca);
            TypedValue typedValue = new TypedValue();

            requireContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);

            binding.readState.getDrawable().setTint(typedValue.data);
            binding.inLibraryText.setText("Acompanhando obra");

        }
        new Thread() {
            @Override
            public void run() {
                model = Model.getInstance(getActivity());
                Instant i = Instant.now();
                model.addOrUpdateReadingHitory(new History(manga, i.getEpochSecond()));
            }
        }.start();
        binding.resumeRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChapterManga c = lastChapter.isEmpty() ? allChapters.get(allChapters.size() - 1) : getChapterFromDataSet(allChapters, lastChapter);

                Bundle bundle = new Bundle();
                mangaDataViewModel.setIdChap(c.getId());
                bundle.putInt("currentPage", c.getCurrentPage());
                InfoMangaFragment.this.navigateToRead(bundle);

            }
        });

//        manga.setChapters(model.getAllChapterByMangaID(manga.getId(), manga.getLanguage()));

        language = manga.getLanguage();

        chaptersAdapter = new AdapterChapters(getActivity(), chapterMangasListed);
        chaptersAdapter.setMangaDataViewModel(mangaDataViewModel);
        chaptersAdapter.setFragment(InfoMangaFragment.this);

        chaptersAdapter.setMangaBitMapLogo(manga.getImage());
        chaptersAdapter.setIdMangaApi(manga.getId());
        chaptersAdapter.setChapterLanguage(manga.getLanguage());
        if (manga.getChapters() != null && !manga.getChapters().isEmpty()) {
            this.stopLoading();
            allChapters = manga.getChapters();

        }



        Glide.with(getActivity())
                .load(manga.getLanguage().equals("pt-br") ? R.drawable.brazil_flag : manga.getLanguage().equals("en") ? R.drawable.usa_flag : R.drawable.spain_flag)
                .override((int) (30 * getActivity().getResources().getDisplayMetrics().density), (int) (30 * getActivity().getResources().getDisplayMetrics().density))
                .into(binding.mangaLanguageFlag);


        new Thread() {
            @Override
            public void run() {
                model = Model.getInstance(requireActivity());
                boolean value = model.mangaAlredFavorited(manga);
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setAdded(value);
                    }
                });
            }
        }.start();
        Glide.with(requireActivity())
                .load(manga.isAdded ? storage.getLogoFromStorage(manga.getId()) : storageTemp.getLogoFromTempStorage(manga.getId()))
                .override((int) (100 * requireActivity().getResources().getDisplayMetrics().density), (int) (140 * requireActivity().getResources().getDisplayMetrics().density))
                .into(binding.logo);

        Glide.with(requireActivity())
                .asBitmap()
                .load(manga.isAdded ? storage.getLogoFromStorage(manga.getId()) : storageTemp.getLogoFromTempStorage(manga.getId()))
                .override(binding.backGroundLogo.getMaxWidth(), binding.backGroundLogo.getHeight())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Drawable backDrawable = new BitmapDrawable(getResources(), resource);
                        backDrawable.setAlpha(120);
                        binding.backGroundLogo.setBackground(backDrawable);

                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
        binding.title.setText(manga.getTitulo());
        binding.extension.setText(binding.extension.getText().toString() + (manga.getId().contains("manganato") || manga.getId().contains("mangakakalot") ? "Mangakakalot" : "MangaDex"));
        binding.descriptionText.setText(manga.getDescricao());
        binding.goBack.bringToFront();
        StringBuilder autores = new StringBuilder();
        for (int i = 0; i < (manga.getAutor() != null ? manga.getAutor().size() : 0); i++) {
            if (i != manga.getAutor().size() - 1) {
                autores.append(manga.getAutor().get(i)).append(", ");
            } else {
                autores.append(manga.getAutor().get(i));
            }
        }
        binding.author.setText(autores);


        RecyclerView recyclerView = binding.genresContainer;


        AdapterGenres adapter = new AdapterGenres(getActivity(), manga.getTags());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);

        binding.goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });


        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(adapter);

        recyclerView.setHasFixedSize(true);

        binding.chapterContainer.setAdapter(chaptersAdapter);
        binding.chapterContainer.setLayoutManager(new LinearLayoutManager(requireActivity()) {
            @Override
            public boolean canScrollVertically() {
                return canScroll;
            }
        });

        binding.scrollParent.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//                System.out.println(String.format("Quantidade scrollada: %d\nQuantidade total: %d",v.getScrollY(),binding.containerInfo.getHeight()));
                if(v.getScrollY() > binding.containerInfo.getHeight()){
                    canScroll = true;
                    return;
                }
                canScroll = false;
            }
        });
        binding.chapterContainer.setHasFixedSize(false);
        binding.chapterContainer.setNestedScrollingEnabled(true);

        MangakakalotExtension.OnMangaLoaded onMangaLoaded = new MangakakalotExtension.OnMangaLoaded() {
            @Override
            public void onMangaLoaded(Manga manga) {
                InfoMangaFragment.this.manga.setDescricao(manga.getDescricao());
                InfoMangaFragment.this.manga.setAutor(manga.getAutor());
                InfoMangaFragment.this.manga.setTags(manga.getTags());
                InfoMangaFragment.this.manga.setLastChapter(manga.getLastChapter());
                requireActivity().runOnUiThread(() -> {
                    binding.descriptionText.setText(manga.getDescricao());
                    StringBuilder autores = new StringBuilder();
                    for (int i = 0; i < (manga.getAutor() != null ? manga.getAutor().size() : 0); i++) {
                        if (i != manga.getAutor().size() - 1) {
                            autores.append(manga.getAutor().get(i)).append(", ");
                        } else {
                            autores.append(manga.getAutor().get(i));
                        }
                    }
                    binding.author.setText(autores);
                    AdapterGenres adapter = new AdapterGenres(getActivity(), manga.getTags());
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
                    recyclerView.setLayoutManager(linearLayoutManager);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setHasFixedSize(true);

                });
            }
        };
        mangaDexExtension = requireActivity() instanceof MangaShowContentActivity ? (((MangaShowContentActivity) requireActivity()).getExtension().equals(Extensions.MANGADEX) ? new MangaDexExtension(this.language, imageQuality) : new MangakakalotExtension(onMangaLoaded)) : new MangaDexExtension(this.language, imageQuality);

        favorite();
        loadChapters();
        showMore();

        binding.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownloadChapterService();
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!chaptersAdapter.getChapterToDownload().isEmpty()) {
                    chaptersAdapter.disablelongPressedItens();
                    controlDownloadButtonVisibility(false);
                    return;
                }
                requireActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);

        binding.sortChapterIcon.setOnClickListener((v)->{
            if(allChapters != null && !allChapters.isEmpty()){
                if(!isOrdenedReverse)allChapters.sort(Comparator.comparingDouble((ChapterManga c)->Double.parseDouble(c.getChapter())));
                else allChapters.sort(Comparator.comparingDouble((ChapterManga c)->Double.parseDouble(c.getChapter())).reversed());
                chaptersAdapter.notifyDataSetChanged();
            }

            if(binding.sortChapterIcon.getDrawable() instanceof AnimatedVectorDrawable){
                ((AnimatedVectorDrawable)binding.sortChapterIcon.getDrawable()).start();
                if(binding.sortChapterIcon.getDrawable() instanceof AnimatedVectorDrawable){
                    if(animationCallback != null){
                        ((AnimatedVectorDrawable)binding.sortChapterIcon.getDrawable()).unregisterAnimationCallback(animationCallback);
                    }
                     animationCallback = new Animatable2.AnimationCallback() {
                        @Override
                        public void onAnimationEnd(Drawable drawable) {

                            super.onAnimationEnd(drawable);
                            if(!isOrdenedReverse){
                                binding.sortChapterIcon.setImageResource(R.drawable.animated_vector_sort_reverse);
                                isOrdenedReverse = true;
                                return;
                            }
                            binding.sortChapterIcon.setImageResource(R.drawable.animated_vector_sort);
                            isOrdenedReverse = false;

                        }
                    };
                    ((AnimatedVectorDrawable)binding.sortChapterIcon.getDrawable()).registerAnimationCallback(animationCallback);
                }

            }
        });

        binding.filterChapters.setOnClickListener((v)->{
            binding.filterChapters.setImageResource(!isFilterChaptersOn?R.drawable.cloud_download_24dp_e8eaed_fill0_wght400_grad0_opsz24:R.drawable.cloud_24dp_e8eaed_fill0_wght400_grad0_opsz24);
            chaptersAdapter.setShowJustDownloadedChapters(!isFilterChaptersOn);
            chaptersAdapter.notifyDataSetChanged();
            isFilterChaptersOn = !isFilterChaptersOn;
            binding.numChapters.setText((isFilterChaptersOn?allChapters.stream().mapToInt((c)->{return c.isDownloaded()?1:0;}).sum():allChapters.size()) + " Capítulos");
        });

        return v;
    }
    @Override
    public void onStart(){
        super.onStart();
        new Thread(this::continueReading).start();
        if(allChapters != null && !allChapters.isEmpty()){
            binding.numChapters.setText(allChapters.size() + " Capítulos");
            binding.sortChapterIcon.setVisibility(View.VISIBLE);
        }
        binding.sortChapterIcon.setImageResource(R.drawable.animated_vector_sort);
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    private void favorite() {
        binding.readState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.readState.getDrawable() instanceof AnimatedVectorDrawable){
                    ((AnimatedVectorDrawable)binding.readState.getDrawable()).start();
                }
                TypedValue typedValue = new TypedValue();
                requireContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
                binding.readState.getDrawable().setTint(typedValue.data);
                binding.inLibraryText.setText("Acompanhando obra");
                workerThread = new Thread() {
                    @Override
                    public void run() {
                        model = Model.getInstance(getActivity());
                        if (unlockAddManga && (manga.getChapters() != null || !allChapters.isEmpty())) {
                            unlockAddManga = false;
                            manga.setChapters(allChapters);

                            if (!isAdded && manga.getChapters() != null) {
                                if (!storage.receiveFile(manga.getId())) return;
                                if (model.addInFavorites(manga)) {
                                    isAdded = true;
                                }
                                unlockAddManga = true;
                                return;
                            }
                            if (!storage.removeLogo(manga.getId())) return;
                            if (model.removeFromFavorites(manga)) {
                                isAdded = false;
                                if(binding.readState.getDrawable() instanceof AnimatedVectorDrawable){
                                    ((AnimatedVectorDrawable)binding.readState.getDrawable()).reset();
                                }
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
//                                        binding.readState.setImageResource(R.drawable.adicionar_na_biblioteca);
                                        TypedValue typedValue = new TypedValue();

                                        requireContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorTertiary, typedValue, true);

                                        binding.readState.getDrawable().setTint(typedValue.data);
                                        binding.inLibraryText.setText("Acompanhar obra");
                                        isAdded = false;
                                    }
                                });
                            }
                            unlockAddManga = true;
                        }
                    }
                };


                workerThread.start();


            }
        });
    }

    private void showMore() {
        binding.showMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // System.out.println("Clicou");
                if (!isTextExpanded) {
                    isTextExpanded = true;
                    binding.descriptionText.setMaxLines(30);
                    binding.showMoreText.setText("↑↑ Mostrar menos ↑↑");
                    binding.genresContainer.setVisibility(View.VISIBLE);
                    return;
                }
                binding.genresContainer.setVisibility(View.GONE);
                isTextExpanded = false;
                binding.descriptionText.setMaxLines(4);
                binding.showMoreText.setText("↓↓ Mostrar mais ↓↓");


            }
        });
    }


    private void loadChapters() {
        if (mangaDataViewModel != null && mangaDataViewModel.getManga() != null && mangaDataViewModel.getManga().getChapters() != null) {
            if (!mangaDataViewModel.getManga().getChapters().isEmpty()) {
                mangaDataViewModel.getManga().getChapters().sort(Comparator.comparingDouble((ChapterManga c)->Double.parseDouble(c.getChapter())).reversed());
                chaptersAdapter = new AdapterChapters(requireActivity(), mangaDataViewModel.getManga().getChapters());
                chaptersAdapter.setFragment(this);
                chaptersAdapter.setMangaDataViewModel(mangaDataViewModel);
                binding.chapterContainer.setAdapter(chaptersAdapter);
                if (manga.getLastChapter() != 0 && !manga.isOngoing(mangaDataViewModel.getManga().getChapters()))
                    binding.status.setText("Concluído");
                return;
            }
        }
        MangaShowContentActivity mangaShowContentActivity = (MangaShowContentActivity) getContext();
//        System.out.println("manga added: "+manga.getLastChapter());
        if (manga.isAdded) {
            chapterMangasListed.clear();
            chaptersAdapter.notifyDataSetChanged();
            binding.progressChapter.setVisibility(View.VISIBLE);

            new Thread() {
                @Override
                public void run() {
                    model = Model.getInstance(getActivity());
                    allChapters = model.getAllChapterByMangaID(manga.getId(), manga.getLanguage());
                    allChapters.sort(Comparator.comparingDouble((ChapterManga ch)->Double.parseDouble(ch.getChapter())).reversed());
                    mangaDataViewModel.getManga().setChapters(allChapters);
                    chapterMangasListed.clear();
                    indexSubLists = 0;
//                    allChapters = chapters;

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chaptersAdapter = new AdapterChapters(requireActivity(), allChapters);
                            chaptersAdapter.setFragment(InfoMangaFragment.this);
                            chaptersAdapter.setMangaDataViewModel(mangaDataViewModel);
                            binding.chapterContainer.setAdapter(chaptersAdapter);
                            if (manga.getLastChapter() != 0 && !manga.isOngoing(allChapters))
                                binding.status.setText("Concluído");
                        }
                    }
                    );
                    continueReading();

                    if (getActivity() == null) {
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            binding.numChapters.setText(allChapters.size() + " Capítulos");
                            binding.sortChapterIcon.setVisibility(View.VISIBLE);
                            binding.progressChapter.setVisibility(View.GONE);

//                            manageSubLists();
                            mangaDataViewModel.getManga().setChapters(allChapters);
                        }
                    });
                }
            }.start();
            return;
        }
//            if (manga.getChapters() == null || manga.getChapters().isEmpty() || !mangaShowContentActivity.fromMain) {
        chapterMangasListed.clear();
        chaptersAdapter.notifyDataSetChanged();
        binding.progressChapter.setVisibility(View.VISIBLE);
        new Thread() {
            @Override
            public void run() {
                if(mangaDataViewModel == null)return;
                allChapters = mangaDataViewModel.getManga().getChapters() != null && !mangaDataViewModel.getManga().getChapters().isEmpty() ? mangaDataViewModel.getManga().getChapters() : (mangaDexExtension instanceof MangaDexExtension?mangaDexExtension.viewChapters(manga.getId()):mangaDexExtension.viewChapters(manga.getId(),mainHandler));
                if(mangaDataViewModel == null)return;
                Objects.requireNonNull(mangaDataViewModel.getManga()).setChapters(allChapters);
                if(mangaDataViewModel == null)return;
                allChapters.sort(Comparator.comparingDouble((ChapterManga c)->Double.parseDouble(c.getChapter())).reversed());
                chapterMangasListed.clear();
                indexSubLists = 0;

                if (getActivity() == null) {
                    return;
                }
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!manga.isOngoing(allChapters)) binding.status.setText("Concluído");
                        binding.numChapters.setText(allChapters.size() + " Capítulos");
                        binding.sortChapterIcon.setVisibility(View.VISIBLE);
                        binding.progressChapter.setVisibility(View.GONE);
                        mangaDataViewModel.getManga().setChapters(allChapters);
                        chaptersAdapter = new AdapterChapters(requireActivity(), allChapters);
                        chaptersAdapter.setMangaDataViewModel(mangaDataViewModel);
                        chaptersAdapter.setFragment(InfoMangaFragment.this);
                        binding.chapterContainer.setAdapter(chaptersAdapter);

                    }
                });


            }
        }.start();
    }

    @Deprecated
    public void setDataSet(ArrayList<ChapterManga> arrayList) {
        chaptersAvaible = true;
        chapterMangasListed.addAll(arrayList);

        binding.numChapters.setText(chapterMangasListed.size() + " Capítulos");

        chapterMangasListed.sort(Comparator.comparingDouble((ChapterManga ch)->Double.parseDouble(ch.getChapter())).reversed());

        isdescendingOrder = true;
        chaptersAdapter.notifyDataSetChanged();
        mangaDataViewModel.getManga().setChapters(arrayList);

    }

    public void stopLoading() {
        binding.progressChapter.setVisibility(View.GONE);
    }

    public void navigateToRead() {
        Navigation.findNavController(v).navigate(R.id.action_infoMangaFragment2_to_readerMangaFragment);

    }

    public void navigateToRead(Bundle bundle) {
        bundle.putString("mangaIdApi", manga.getId());
        bundle.putString("mangaLanguage", manga.getLanguage());
        Navigation.findNavController(v).navigate(R.id.action_infoMangaFragment2_to_readerMangaFragment, bundle);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.indexSubLists = 0;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mangaDataViewModel != null)mangaDataViewModel.setManga(null);
        mangaDataViewModel = null;

    }

    private ChapterManga getChapterFromDataSet(ArrayList<ChapterManga> data, String
            idChapter) {
        for (ChapterManga c : data) {

            if (c.getId().equals(idChapter)) {

                return c;
            }
        }
        return null;
    }

    public void controlDownloadButtonVisibility(boolean isDownloadVisible) {
        ValueAnimator valueAnimator;

        valueAnimator = isDownloadVisible ? ValueAnimator.ofInt(-55, 20) : ValueAnimator.ofInt(20, -55);
        ;
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(animation -> {
            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) binding.downloadButton.getLayoutParams();
            lp.rightMargin = (int) ((int) animation.getAnimatedValue() * getActivity().getResources().getDisplayMetrics().density);
            binding.downloadButton.setLayoutParams(lp);
        });


        valueAnimator.start();
    }

    private void startDownloadChapterService() {
        String[] chaptersID = new String[chaptersAdapter.getChapterToDownload().size()];
        int index = 0;
//        chaptersAdapter.disablelongPressedItens();
        controlDownloadButtonVisibility(false);
        for (ChapterManga ch : chaptersAdapter.getChapterToDownload()) {
            chaptersID[index] = ch.getId();
            index++;
        }

        Data data = new Data.Builder()
                .putStringArray("chaptersID", chaptersID)
                .putString("language", manga.getLanguage())
                .putString("folderChapterName", manga.getId())
                .putString("extension", mangaDexExtension instanceof MangaDexExtension?Extensions.MANGADEX:Extensions.MANGAKAKALOT).build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(DownloadChapterWork.class)
                .addTag(ActionsPending.DOWNLOAD_CHAPTER_TAG)
                .setInputData(data).build();

        workManager.enqueueUniqueWork("DownloadChapters", ExistingWorkPolicy.KEEP, workRequest);
        workManager.getWorkInfoByIdLiveData(workRequest.getId()).observe(requireActivity(), new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                if (workInfo == null) return;
                if (workInfo.getState().isFinished()) {
                    String[] ids = workInfo.getOutputData().getStringArray("ids");
                    chaptersAdapter.setDownloadSucecessful(ids);
                }
            }
        });
    }

    public void setAdded(boolean added) {
        isAdded = added;
        if (added) {

//            binding.readState.setImageResource(R.drawable.adicionado_a_biblioteca);
            TypedValue typedValue = new TypedValue();

            requireContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);

            binding.readState.getDrawable().setTint(typedValue.data);
            binding.inLibraryText.setText("Acompanhando obra");
        }

    }
    @WorkerThread
    public void continueReading(){
        lastChapter = model.getIdApiOfLastChapterRead(manga.getId(), manga.getLanguage());
        if (!lastChapter.isEmpty()) {

            requireActivity().runOnUiThread(()->{
                TypedValue typedValue = new TypedValue();

                requireContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);

                binding.resumeRead.getDrawable().setTint(typedValue.data);
                if(this.binding.resumeRead.getDrawable() instanceof AnimatedVectorDrawable){
                    ((AnimatedVectorDrawable) this.binding.resumeRead.getDrawable()).start();
                }
                ChapterManga chapterManga = getChapterFromDataSet(allChapters, lastChapter);
                if (chapterManga == null) return;

                binding.resumeState.setText("Continuar leitura do capítulo " + chapterManga.getChapter());
            });
        }
    }
    public boolean isMangaAdded() {
        return isAdded;
    }
}