package com.example.trinity.fragments;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionInflater;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.trinity.ExtensionShowContentActivity;
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
        adapter = new AdapterMangas(getActivity(), new ArrayList<Manga>(),this).setShowLanguageIcon(true).setShowAmountChapterToRead(true);

        recyclerView.setAdapter(adapter);
        myActivity = (MainActivity) getActivity();

        binding.searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                new Thread() {
                    @Override
                    public void run() {
                        model.loadSearch(s.toString(), LibraryFragment.this);
                    }
                }.start();
            }
        });
        binding.searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchFildIsShowed) {

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
//        this.mangasFromDataBaseViewModel.getMangaMutableLiveData().observe(getActivity(), new Observer<ArrayList<Manga>>() {
//            @Override
//            public void onChanged(ArrayList<Manga> mangas) {
//                recyclerView.setAdapter(new AdapterMangas(getActivity(), mangasFromDataBaseViewModel.getMangas(),LibraryFragment.this).setShowLanguageIcon(true).setShowAmountChapterToRead(true));
//                new Thread(){
//                    @Override
//                    public void run(){
//                        for (int i = 0; i < mangasFromDataBaseViewModel.getMangas().size(); i++) {
//                            int amount = model.getAmountChaptersToRead(mangasFromDataBaseViewModel.getMangas().get(i).getId(), mangasFromDataBaseViewModel.getMangas().get(i).getLanguage());
//                            if (mangasFromDataBaseViewModel.getMangas().get(i).getAmountChaptersToRead() > amount) {
//                                mangasFromDataBaseViewModel.getMangas().get(i).setAmountChaptersToRead(amount);
//                                if (adapter != null) {
//                                    int finalI = i;
//
//                                    getActivity().runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            adapter.notifyItemChanged(finalI);
//                                        }
//                                    });
//
//
//                                }
//                            }
//                        }
//                    }
//                }.start();
//            }
//        });
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
    public void onResume() {
        super.onResume();


        mangasFromDataBaseViewModel.getMangaMutableLiveData().observe(getActivity(), new Observer<ArrayList<Manga>>() {
            @Override
            public void onChanged(ArrayList<Manga> mangas) {
//                System.out.println("changed");
                if(mangas.size() != adapter.getItemCount()){
                    System.out.println("changed");
                    adapter = new AdapterMangas(getActivity(), mangasFromDataBaseViewModel.getMangas(),LibraryFragment.this).setShowLanguageIcon(true).setShowAmountChapterToRead(true);
                    adapter.setFragment(LibraryFragment.this);
                    adapter.setShowLanguageIcon(true);
                    recyclerView.setAdapter(adapter);
                }
            }
        });



        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.controllShowBottomNavigator(this);
            mainActivity.isInFirstDestination = true;
            mainActivity.isInReadFragment = false;
        }

        new Thread() {
            @Override
            public void run() {
                model = Model.getInstance(requireActivity());
                if (model.getMangaCount() != dataSet.size()) {
                    dataSet = model.selectAllMangas(false);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("é maior");
                            mangasFromDataBaseViewModel.setMangas(dataSet);
                        }
                    });
                }
//                for (int i = 0; i < dataSet.size(); i++) {
//                    int amount = model.getAmountChaptersToRead(dataSet.get(i).getId(), dataSet.get(i).getLanguage());
////                    System.out.println(amount);
//                    if (dataSet.get(i).getAmountChaptersToRead() > amount) {
//                        dataSet.get(i).setAmountChaptersToRead(amount);
//                        if (adapter != null) {
//                            int finalI = i;
//
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    adapter.notifyItemChanged(finalI);
//                                }
//                            });
//
//
//                        }
//                    }
//                }
            }
        }.start();


    }

    public void searchResult(ArrayList<Manga> mangas) {
        adapter = new AdapterMangas(getActivity(), mangas,this).setShowLanguageIcon(true).setShowAmountChapterToRead(true);

        recyclerView.setAdapter(adapter);
    }


}