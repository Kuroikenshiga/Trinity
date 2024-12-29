package com.example.trinity.dialogs;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.trinity.R;
import com.example.trinity.adapters.AdapterChapters;
import com.example.trinity.adapters.AdapterThemes;
import com.example.trinity.databinding.SelectThemeDialogLayoutBinding;
import com.example.trinity.preferecesConfig.ConfigClass;

import java.util.ArrayList;
import java.util.Objects;

public class ThemeChangeDialogFragment extends DialogFragment {

    private SelectThemeDialogLayoutBinding binding;
    private ValueAnimator animator;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SelectThemeDialogLayoutBinding.inflate(inflater,container,false);
        startUpDialogFragment();
        animationStartUp();
        binding.selectTheme.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if(position != 0 && animator != null && animator.isRunning()){
                    animator.cancel();
                    binding.animation.setVisibility(View.INVISIBLE);
                    binding.animationLabel.setVisibility(View.INVISIBLE);
                }
            }
        });

        return binding.getRoot();
    }

    private void animationStartUp(){
        animator = ValueAnimator.ofInt(-35,35);
        animator.setDuration(500);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);

        animator.addUpdateListener((animation -> {
            binding.animation.setRotation((int)animation.getAnimatedValue());
        }));

        animator.start();
    }

    private void startUpDialogFragment(){

        ArrayList<ContentValues> values = new ArrayList<>();

        ContentValues contentValues = new ContentValues();
        contentValues.put(AdapterThemes.PRIMARY_COLOR,requireActivity().getColor(R.color.colorPrimaryT1));
        contentValues.put(AdapterThemes.SECONDARY_COLOR,requireActivity().getColor(R.color.colorSecondaryT1));
        contentValues.put(AdapterThemes.SURFACE_COLOR,requireActivity().getColor(R.color.colorSurfaceT1));
        contentValues.put(AdapterThemes.TERTIARY_COLOR,requireActivity().getColor(R.color.colorTertiaryT1));
        values.add(contentValues);

        contentValues = new ContentValues();
        contentValues.put(AdapterThemes.PRIMARY_COLOR,requireActivity().getColor(R.color.colorPrimaryT2));
        contentValues.put(AdapterThemes.SECONDARY_COLOR,requireActivity().getColor(R.color.colorSecondaryT2));
        contentValues.put(AdapterThemes.SURFACE_COLOR,requireActivity().getColor(R.color.colorSurfaceT2));
        contentValues.put(AdapterThemes.TERTIARY_COLOR,requireActivity().getColor(R.color.colorTertiaryT2));
        values.add(contentValues);

        contentValues = new ContentValues();
        contentValues.put(AdapterThemes.PRIMARY_COLOR,requireActivity().getColor(R.color.colorPrimaryT3));
        contentValues.put(AdapterThemes.SECONDARY_COLOR,requireActivity().getColor(R.color.colorSecondaryT3));
        contentValues.put(AdapterThemes.SURFACE_COLOR,requireActivity().getColor(R.color.colorSurfaceT3));
        contentValues.put(AdapterThemes.TERTIARY_COLOR,requireActivity().getColor(R.color.colorTertiaryT3));
        values.add(contentValues);

        contentValues = new ContentValues();
        contentValues.put(AdapterThemes.PRIMARY_COLOR,requireActivity().getColor(R.color.colorPrimaryT4));
        contentValues.put(AdapterThemes.SECONDARY_COLOR,requireActivity().getColor(R.color.colorSecondaryT4));
        contentValues.put(AdapterThemes.SURFACE_COLOR,requireActivity().getColor(R.color.colorSurfaceT4));
        contentValues.put(AdapterThemes.TERTIARY_COLOR,requireActivity().getColor(R.color.colorTertiaryT4));
        values.add(contentValues);

        binding.selectTheme.setAdapter(new AdapterThemes(values,requireContext()));

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(ConfigClass.TAG_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        binding.confirm.setOnClickListener((v)->{

            switch (binding.selectTheme.getCurrentItem()){
                case 0:
                    editor.putString(ConfigClass.ConfigTheme.THEME,ConfigClass.ConfigTheme.THEME_DEFAULT);
                    break;
                case 1:
                    editor.putString(ConfigClass.ConfigTheme.THEME,ConfigClass.ConfigTheme.THEME_MODERN_ELEGANCE);
                    break;
                case 2:
                    editor.putString(ConfigClass.ConfigTheme.THEME,ConfigClass.ConfigTheme.THEME_VIOLET_IRIS);
                    break;
                default:
                    editor.putString(ConfigClass.ConfigTheme.THEME,ConfigClass.ConfigTheme.THEME_EMERALD_HAVEN);
                    break;
            }
            editor.apply();
            requireActivity().recreate();
        });
        binding.cancel.setOnClickListener((v)->{
            dismiss();
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialog_shape);

        Objects.requireNonNull(getDialog()).getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
