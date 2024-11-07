package com.example.trinity.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.trinity.MainActivity;
import com.example.trinity.R;
import com.example.trinity.adapters.AdapterHistory;
import com.example.trinity.databinding.FragmentHistoryMangaBinding;
import com.example.trinity.models.Model;
import com.example.trinity.valueObject.History;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryMangaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryMangaFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentHistoryMangaBinding binding;
    private Model model;
    private ArrayList<History> histories;
    private AdapterHistory adapterHistory;
    public HistoryMangaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryMangaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryMangaFragment newInstance(String param1, String param2) {
        HistoryMangaFragment fragment = new HistoryMangaFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.binding = FragmentHistoryMangaBinding.inflate(inflater,container,false);
        View v = binding.getRoot();
        new Thread(){
            @Override
            public void run(){
                model = Model.getInstance(getActivity());
                histories = model.selectAllHistory();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapterHistory = new AdapterHistory(getActivity(),histories,HistoryMangaFragment.this);
                        binding.reciclerViewHistory.setAdapter(adapterHistory);
                        binding.reciclerViewHistory.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL,false));
                    }
                });
            }
        }.start();


        return v;
    }
    @Override
    public void onResume() {
        super.onResume();
        if(getActivity() instanceof MainActivity){
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.controllShowBottomNavigator(this);
            mainActivity.isInFirstDestination = false;
            mainActivity.isInReadFragment = false;
        }
    }
    @Override
    public void onDestroy(){
        super.onDestroy();

    }
}