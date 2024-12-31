package com.example.trinity.fragments.transitionFragments;

import android.animation.ValueAnimator;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.trinity.MangaShowContentActivity;
import com.example.trinity.databinding.FragmentLoadingBinding;
import com.example.trinity.models.Model;
import com.example.trinity.valueObject.ChapterManga;
import com.example.trinity.viewModel.MangaDataViewModel;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoadingChaptersToReadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoadingChaptersToReadFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ValueAnimator animatorLeft;
    ValueAnimator animatorRight;
    private FragmentLoadingBinding binding;
    private Thread workerThread;
    public LoadingChaptersToReadFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoadingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoadingChaptersToReadFragment newInstance(String param1, String param2) {
        LoadingChaptersToReadFragment fragment = new LoadingChaptersToReadFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLoadingBinding.inflate(inflater,container,false);
        animationStartUp();
        MangaDataViewModel viewModel = new ViewModelProvider(requireActivity()).get(MangaDataViewModel.class);
        this.workerThread = new Thread(()->{
            Model model  = Model.getInstance(requireActivity());
            ArrayList<ChapterManga> chapterMangas = model.getAllChapterByMangaID(viewModel.getManga().getId(),viewModel.getManga().getLanguage());
            requireActivity().runOnUiThread(()->{
                viewModel.getManga().setChapters(chapterMangas);
                ((MangaShowContentActivity)requireActivity()).toReaderFragment();
            });
        });
        workerThread.start();


        return this.binding.getRoot();
    }
    private void animationStartUp() {
//        if(!isLoadingNewChapter)return;
        LinearLayout.LayoutParams layoutParamsLeft = (LinearLayout.LayoutParams) binding.left.getLayoutParams();
        LinearLayout.LayoutParams layoutParamsRight = (LinearLayout.LayoutParams) binding.right.getLayoutParams();

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
    public void onDestroy() {
        super.onDestroy();
        if(animatorLeft.isRunning() || animatorRight.isRunning()){
            animatorLeft.cancel();
            animatorRight.cancel();
        }
        if(workerThread.isAlive() && workerThread != null){
            workerThread.interrupt();
        }
    }
}