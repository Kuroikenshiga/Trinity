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

import com.example.trinity.MainActivity;
import com.example.trinity.R;
import com.example.trinity.adapters.AdapterMangas;


import com.example.trinity.databinding.FragmentLibraryBinding;
import com.example.trinity.models.Model;
import com.example.trinity.valueObject.Manga;
import com.example.trinity.viewModel.MangasFromDataBaseViewModel;

import java.util.ArrayList;
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
    private Model model;
    private Handler mainHandle;
    private boolean searchFildIsShowed = false;
    MangasFromDataBaseViewModel mangasFromDataBaseViewModel;
    private boolean isSearching = false;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        this.mangasFromDataBaseViewModel = new ViewModelProvider(getActivity()).get(MangasFromDataBaseViewModel.class);
        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        recyclerView = binding.reciclerViewMangas;
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        adapter = new AdapterMangas(getActivity(), this.mangasFromDataBaseViewModel.getMangas(),this).setShowLanguageIcon(true).setShowAmountChapterToRead(true);

        mangasFromDataBaseViewModel.getMangaMutableLiveData().observe(requireActivity(), new Observer<ArrayList<Manga>>() {
            @Override
            public void onChanged(ArrayList<Manga> mangas) {
                adapter = new AdapterMangas(requireActivity(),mangas);
                recyclerView.setAdapter(adapter);
            }
        });

        recyclerView.setAdapter(adapter);

        loadLibrary();


        myActivity = (MainActivity) getActivity();

//        binding.searchField.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                new Thread() {
//                    @Override
//                    public void run() {
//                        model.loadSearch(s.toString(), LibraryFragment.this);
//                    }
//                }.start();
//            }
//        });

        binding.searchField.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if(keyCode == KeyEvent.KEYCODE_ENTER){
                    new Thread(()->{
                        if(isSearching)return;
                        isSearching = true;
                        ArrayList<Manga>listResult = model.loadSearch(binding.searchField.getText().toString(), null);
                        requireActivity().runOnUiThread(()->{
                            mangasFromDataBaseViewModel.getMangaMutableLiveData().setValue(listResult);
                        });
                        isSearching = false;
                    }).start();

                }
                return false;
            }
        });
        binding.searchAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(()->{
                    if(isSearching)return;
                    isSearching = true;
                    model.loadSearch(binding.searchField.getText().toString(), LibraryFragment.this);
                    isSearching = false;
                }).start();
            }
        });
        binding.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.searchField.setText("");
                new Thread(()->{
                    if(isSearching)return;
                    isSearching = true;
                    model.loadSearch(binding.searchField.getText().toString(), LibraryFragment.this);
                    isSearching = false;
                }).start();
            }
        });
        binding.searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchFildIsShowed) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        binding.searchField.getWindowInsetsController().show(WindowInsetsCompat.Type.ime());
                    }
                    searchFildIsShowed = true;

                    ValueAnimator show = ValueAnimator.ofInt(0, -60);
                    show.setDuration(500);

                    show.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                            LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams) binding.titleContainer.getLayoutParams();
                            lp1.topMargin = (int)(((int)animation.getAnimatedValue())*getActivity().getResources().getDisplayMetrics().density);
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

                    hidden.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                            LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams) binding.titleContainer.getLayoutParams();
                            lp1.topMargin = (int)(((int)animation.getAnimatedValue())*getActivity().getResources().getDisplayMetrics().density);
                            binding.titleContainer.setLayoutParams(lp1);

                        }
                    });
                    hidden.start();

                    binding.searchField.setText("");
//                    new Thread(()->{
//                        if(isSearching)return;
//                        isSearching = true;
//                        model.loadSearch(binding.searchField.getText().toString(), null);
//                        isSearching = false;
//                    }).start();
                    loadLibrary(true);

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
        getActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);
//
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        binding = null;

    }
    @Override
    public void onPause(){
        super.onPause();
        if(searchFildIsShowed){
            LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams) binding.titleContainer.getLayoutParams();
            lp1.topMargin = 0;
            binding.titleContainer.setLayoutParams(lp1);
            searchFildIsShowed = false;

        }

    }

    @Override
    public void onStart(){
        super.onStart();
//
        loadLibrary();
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

    }

    private void loadLibrary(){

        if(!mangasFromDataBaseViewModel.getMangas().isEmpty()){
            return;
        }
        new Thread(()->{
            model = Model.getInstance(requireActivity());

            dataSet = model.selectAllMangas(false);
            requireActivity().runOnUiThread(()->{
                if(model.getObserversSize() < 1){
                    model.addOnMangaRemovedListener(new Model.OnMangaRemovedListener() {
                        @Override
                        public int getOwner() {
                            return Model.OnMangaRemovedListener.LIBRARY_OWNER;
                        }
                        @Override
                        public void onMangaRemoved(String language, String idAPI) {
                            for(int i = 0;i <mangasFromDataBaseViewModel.getMangas().size();i++){
                                if(mangasFromDataBaseViewModel.getMangas().get(i).getId().equals(idAPI) && mangasFromDataBaseViewModel.getMangas().get(i).getLanguage().equals(language)){
                                    mangasFromDataBaseViewModel.getMangas().remove(i);
                                    adapter.notifyItemRemoved(i);
                                    break;
                                }
                            }
                        }

                    });
                }
                model.addNotifier(new Model.OnMangaAddedNotifier() {
                    @Override
                    public int getOwner() {
                        return Model.OnMangaAddedNotifier.LIBRARY_OWNER;
                    }

                    @Override
                    public void someMangaAdded() {
                        loadLibrary(true);
                    }
                });
                mangasFromDataBaseViewModel.setMangas(dataSet);
                adapter.setDataSet(mangasFromDataBaseViewModel.getMangas());
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }
    private void loadLibrary(boolean forceLoad){

        new Thread(()->{
            model = Model.getInstance(requireActivity());

            dataSet = model.selectAllMangas(false);
            requireActivity().runOnUiThread(()->{

                mangasFromDataBaseViewModel.setMangas(dataSet);
                adapter.setDataSet(mangasFromDataBaseViewModel.getMangas());
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }
}