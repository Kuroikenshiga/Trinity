package com.example.trinity.fragments;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.trinity.R;
import com.example.trinity.databinding.FragmentReaderConfigsBinding;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReaderConfigsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReaderConfigsFragment extends Fragment {

    private FragmentReaderConfigsBinding binding;
    private boolean[] radio;
    private AppCompatImageView[][] imageViews;
    private OnReadModeSelected onReadModeSelected;
    public static final int STANDARD = 0;
    public static final int REVERSE = 1;
    public static final int CASCADE = 2;
    private Runnable runnable;
    OnSeekBarChangeListener onSeekBarChangeListener;
    public ReaderConfigsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ReaderConfigsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReaderConfigsFragment newInstance() {
        ReaderConfigsFragment fragment = new ReaderConfigsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentReaderConfigsBinding.inflate(inflater, container, false);
        radio = new boolean[]{true,false,false};
        imageViews = new AppCompatImageView[][]{{binding.dir1,binding.pan1},{binding.dir2,binding.pan2},{binding.dir3,binding.pan3}};
        selectedRadioButton();
        configSeekbar();
        binding.alphaController.setStartText("0");
        binding.alphaController.setEndText("100");
        return binding.getRoot();
    }
    private void selectedRadioButton(){
        binding.standard.setOnClickListener((v)->{
            radio[0] = true;radio[1] = false;radio[2] = false;
            configViewRadioButton(0);
            if(onReadModeSelected != null)onReadModeSelected.onStandardModeSelected();
        });
        binding.reverse.setOnClickListener((v)->{
            radio[0] = false;radio[1] = true;radio[2] = false;
            configViewRadioButton(1);
            if(onReadModeSelected != null)onReadModeSelected.onReverseModeSelected();
        });
        binding.cascade.setOnClickListener((v)->{
            radio[0] = false;radio[1] = false;radio[2] = true;
            configViewRadioButton(2);
            if(onReadModeSelected != null)onReadModeSelected.onCascadeModeSelected();
        });
    }
    private void configViewRadioButton(int indexButton){
        if(indexButton >= radio.length)return;

        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorTertiary, typedValue, true);

        for(int i = 0;i < imageViews.length;i++){
            for(int j = 0;j < imageViews[0].length;j++){
                imageViews[i][j].getDrawable().setTint(typedValue.data);
            }
        }
        requireContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        for(int i = 0;i < imageViews[indexButton].length;i++) {
            imageViews[indexButton][i].getDrawable().setTint(typedValue.data);
        }
    }

    public void setOnReadModeSelected(OnReadModeSelected onReadModeSelected) {
        this.onReadModeSelected = onReadModeSelected;
    }

    public void setRadioValue(int readMode){
        if(readMode < 0 || readMode > 2)return;
        configViewRadioButton(readMode);
    }

    public interface OnReadModeSelected{
        void onStandardModeSelected();
        void onReverseModeSelected();
        void onCascadeModeSelected();
    }
    public interface OnSeekBarChangeListener{
        void onChanged(int value);
        void onEndChange(int value);

    }
    private void configSeekbar(){
        binding.alphaController.setPositionListener((F)->{
            System.out.println(binding.alphaController.getBubbleText());
            if(onSeekBarChangeListener != null)onSeekBarChangeListener.onChanged((int)(binding.alphaController.getPosition()*100));
            return Unit.INSTANCE;
        });
        binding.alphaController.setEndTrackingListener(()->{
            if(onSeekBarChangeListener != null)onSeekBarChangeListener.onEndChange((int)(binding.alphaController.getPosition()*100));
            return Unit.INSTANCE;
        });
    }
    public void setAlphaValue(float f){
        binding.alphaController.setPosition(f);

    }
    public void setOnSeekBarChangeListener (OnSeekBarChangeListener onSeekBarChangeListener){
        this.onSeekBarChangeListener = onSeekBarChangeListener;

    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
        if(runnable != null){
            binding.download.setOnClickListener((v)->{
                runnable.run();
            });
        }
    }
}