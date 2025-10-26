package com.example.trinity.fragments;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionInflater;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.trinity.ExtensionShowContentActivity;
import com.example.trinity.MainActivity;
import com.example.trinity.R;
import com.example.trinity.adapters.AdapterMangas;


import com.example.trinity.adapters.AdapterSearchItensResult;
import com.example.trinity.databinding.FragmentLibraryBinding;
import com.example.trinity.models.Model;
import com.example.trinity.valueObject.Manga;
import com.example.trinity.viewModel.MangasFromDataBaseViewModel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LibraryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LibraryFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentLibraryBinding binding;
    private MainActivity myActivity;
    private AdapterMangas adapter;
    private RecyclerView recyclerView;
    private ArrayList<Manga> dataSet = new ArrayList<>();
    private ArrayList<Manga> resultSet;
    private Model model;
    private Timer timer;
    private int LIMIT = 21,OFF_SET = 21;
    private boolean isTimerCanceled = false;
    private long last_time_key_pressed = 0;
    private boolean search_has_change = false;
    private boolean searchFildIsShowed = false;
    MangasFromDataBaseViewModel mangasFromDataBaseViewModel;
    private boolean isSearching = false;
    private boolean supressManyLoad = false;
    private boolean isLoadingLibrary = false;
    public LibraryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LibraryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LibraryFragment newInstance(String param1, String param2) {
        LibraryFragment fragment = new LibraryFragment();
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
        TransitionInflater inflater = TransitionInflater.from(getActivity());
        setExitTransition(inflater.inflateTransition(R.transition.fragment_transition));
    }


    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        this.mangasFromDataBaseViewModel = new ViewModelProvider(requireActivity()).get(MangasFromDataBaseViewModel.class);
        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        recyclerView = binding.reciclerViewMangas;
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        adapter = new AdapterMangas(getActivity(), this.mangasFromDataBaseViewModel.getMangas(), this).setShowLanguageIcon(true).setShowAmountChapterToRead(true);
        timer = new Timer();
        mangasFromDataBaseViewModel.getMangaMutableLiveData().observe(requireActivity(), new Observer<ArrayList<Manga>>() {
            @Override
            public void onChanged(ArrayList<Manga> mangas) {
                adapter = new AdapterMangas(requireActivity(), mangas);
                recyclerView.setAdapter(adapter);
            }
        });

        recyclerView.setAdapter(adapter);
        this.resultSet = new ArrayList<>();

        AdapterSearchItensResult adapterSearchItensResult = new AdapterSearchItensResult(resultSet, requireContext());
        binding.searchItens.setAdapter(adapterSearchItensResult);
        binding.searchItens.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        myActivity = (MainActivity) getActivity();

        binding.searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search_has_change = true;
                last_time_key_pressed = Instant.now().toEpochMilli();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        binding.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.searchField.setText("");
                isTimerCanceled = true;
            }
        });
        binding.searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchFildIsShowed) {

                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void run() {

                            if (model != null && search_has_change && !Objects.requireNonNull(binding.searchField.getText()).toString().isEmpty() && !isSearching && Instant.now().toEpochMilli() - last_time_key_pressed > 700) {
                                search_has_change = false;
                                isSearching = true;
                                resultSet = model.loadSearch(Objects.requireNonNull(binding.searchField.getText()).toString(), 5);
                                adapterSearchItensResult.setData(resultSet);
                                requireActivity().runOnUiThread(()->{
                                    adapterSearchItensResult.notifyDataSetChanged();
                                    binding.searchItens.setVisibility(View.VISIBLE);
                                });
                                isSearching = false;

                                return;
                            }

                        }
                    }, 1500,1000);
                    isTimerCanceled = false;
                    searchFildIsShowed = true;
                    binding.searchItens.setVisibility(View.VISIBLE);
                    ValueAnimator show = ValueAnimator.ofInt(0, -60);
                    show.setDuration(500);

                    show.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                            LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams) binding.titleContainer.getLayoutParams();
                            lp1.topMargin = (int) (((int) animation.getAnimatedValue()) * getActivity().getResources().getDisplayMetrics().density);
                            binding.titleContainer.setLayoutParams(lp1);

                        }
                    });

                    show.start();
                }
            }
        });
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchFildIsShowed) {
                    searchFildIsShowed = false;
                    ValueAnimator hidden = ValueAnimator.ofInt(-60, 0);
                    hidden.setDuration(500);
                    binding.searchItens.setVisibility(View.GONE);
                    if (!isTimerCanceled) {
                        timer.cancel();
                        isTimerCanceled = true;
                    }
                    hidden.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                            LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams) binding.titleContainer.getLayoutParams();
                            lp1.topMargin = (int) (((int) animation.getAnimatedValue()) * getActivity().getResources().getDisplayMetrics().density);
                            binding.titleContainer.setLayoutParams(lp1);

                        }
                    });
                    hidden.start();

                    binding.searchField.setText("");

                    return;
                }
                if (((MainActivity) getActivity()).isInReadFragment) {
                    ((MainActivity) getActivity()).navigateToUpdates();
                    return;
                }
                if (!((MainActivity) getActivity()).isInFirstDestination) {
                    ((MainActivity) getActivity()).navigateToLibrary();
                    return;
                }

                getActivity().finish();


            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), callback);

        binding.reciclerViewMangas.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(!recyclerView.canScrollVertically(1) && !supressManyLoad){

                    supressManyLoad = true;
                    loadLibrary(LIMIT,OFF_SET);
                }
            }
        });

        binding.backToTop.setOnClickListener((v)->{
            forceLoadLibrary(true);
            OFF_SET = 21;
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        binding = null;

    }

    @Override
    public void onPause() {
        super.onPause();
        if (searchFildIsShowed) {
            LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams) binding.titleContainer.getLayoutParams();
            lp1.topMargin = 0;
            binding.titleContainer.setLayoutParams(lp1);
            searchFildIsShowed = false;

        }
        binding.searchItens.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.controllShowBottomNavigator(this);
            mainActivity.isInFirstDestination = true;
            mainActivity.isInReadFragment = false;
        }
        model = Model.getInstance(requireContext());
        forceLoadLibrary(false);

    }

    private void loadLibrary(int limit, int offSet) {
        if(mangasFromDataBaseViewModel.getMangas().size() > 50){
            binding.backToTop.setVisibility(View.VISIBLE);
            mangasFromDataBaseViewModel.getMangas().subList(0, 25).clear();
            adapter.notifyItemRangeRemoved(0,25);
        }
        if(isLoadingLibrary)return;

        isLoadingLibrary = true;

        new Thread(() -> {
            model = Model.getInstance(requireActivity());
            dataSet = model.selectAllMangas(false,limit,offSet);

            if(dataSet.isEmpty()){
                isLoadingLibrary = false;
                return;
            }


            requireActivity().runOnUiThread(() -> {
                int indexStart = mangasFromDataBaseViewModel.getMangas().size();
                mangasFromDataBaseViewModel.getMangas().addAll(dataSet);
                adapter.notifyItemRangeInserted(indexStart,dataSet.size());
                supressManyLoad = false;
                OFF_SET += LIMIT;
                isLoadingLibrary = false;

            });
        }).start();

    }

    private void forceLoadLibrary(boolean ignoreTableMangaChangesContraint) {
        if(!ignoreTableMangaChangesContraint && !Model.getInstance(requireContext()).mangaTableHasChanges())return;

        if(isLoadingLibrary)return;
        isLoadingLibrary = true;
        new Thread(() -> {
            model = Model.getInstance(requireActivity());
            dataSet = model.selectAllMangas(false,LIMIT,0);
            requireActivity().runOnUiThread(() -> {
                if(!this.isAdded()){
                    isLoadingLibrary = false;
                    return;
                }
                mangasFromDataBaseViewModel.setMangas(dataSet);
                adapter.setDataSet(mangasFromDataBaseViewModel.getMangas());
                recyclerView.setAdapter(adapter);
                OFF_SET = 21;
                binding.backToTop.setVisibility(View.GONE);
                isLoadingLibrary = false;

            });
        }).start();
    }
}