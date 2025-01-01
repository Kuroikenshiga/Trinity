package com.example.trinity.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.transition.TransitionInflater;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.trinity.MainActivity;
import com.example.trinity.R;
import com.example.trinity.adapters.AdapterThemes;
import com.example.trinity.databinding.FragmentSettingsBinding;
import com.example.trinity.databinding.SelectThemeDialogLayoutBinding;
import com.example.trinity.dialogs.ThemeChangeDialogFragment;
import com.example.trinity.extensions.MangaDexExtension;
import com.example.trinity.models.Model;
import com.example.trinity.preferecesConfig.ConfigClass;
import com.example.trinity.storageAcess.ChapterStorageManager;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentSettingsBinding binding;
    private boolean isShworeadOrientation = false;
    private boolean isShworeadImageQuality = false;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor preferencesEditor;
    Model model;
    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        sharedPreferences = getActivity().getSharedPreferences(ConfigClass.TAG_PREFERENCE, MODE_PRIVATE);
        preferencesEditor = sharedPreferences.edit();
        int orientation = sharedPreferences.getInt(ConfigClass.ConfigReader.READ_DIRECTION,1);
        String imageQuality = sharedPreferences.getString(ConfigClass.ConfigContent.IMAGE_QUALITY,"dataSaver");

//        binding.alwaysCascate.setChecked(sharedPreferences.getBoolean(ConfigClass.ConfigReader.ALWAYS_CASCADE_WHEN_LONG_STRIP,false));


        if(orientation == 1){
            binding.leftToRight.setChecked(true);
        } else if (orientation == 2) {
            binding.rightToLeft.setChecked(true);
        }
        else{
            binding.cascade.setChecked(true);
        }

//        if(imageQuality.equals("dataSaver")){
//            binding.low.setChecked(true);
//        }
//        else{
//            binding.high.setChecked(true);
//        }
//        binding.alwaysCascate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                preferencesEditor.putBoolean(ConfigClass.ConfigReader.ALWAYS_CASCADE_WHEN_LONG_STRIP,isChecked);
//                preferencesEditor.apply();
//            }
//        });
        binding.readOrientation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ValueAnimator animator = !isShworeadOrientation?ValueAnimator.ofInt(40,180):ValueAnimator.ofInt(180,40);
                animator.setDuration(300);

                ValueAnimator rotateAnimator = !isShworeadOrientation?ValueAnimator.ofFloat(0f,180f):ValueAnimator.ofFloat(180f,0f);
                rotateAnimator.setDuration(300);

                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                       ViewGroup.LayoutParams lp = (ConstraintLayout.LayoutParams) binding.container1.getLayoutParams();
                       lp.height = (int)((int)animation.getAnimatedValue()*getActivity().getResources().getDisplayMetrics().density);
                       binding.container1.setLayoutParams(lp);
                    }
                });

                rotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                        binding.drop1.setRotation((float) animation.getAnimatedValue());
                    }
                });

                animator.start();
                rotateAnimator.start();
                isShworeadOrientation = !isShworeadOrientation;
            }

        });

        binding.changeTheme.setOnClickListener((view)->{
            ThemeChangeDialogFragment dialogFragment = new ThemeChangeDialogFragment();
            dialogFragment.show(getParentFragmentManager(),"ThemeChangeDialogFragment");
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                ((MainActivity)requireActivity()).navigateToLibrary();
            }
        });

        binding.deleteChapters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.confirmDelete.setVisibility(View.VISIBLE);
            }
        });
        binding.confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChapterStorageManager storageManager = new ChapterStorageManager(getActivity());
                binding.confirmDelete.setVisibility(View.GONE);
                new Thread(){
                    @Override
                    public void run(){
                        if(storageManager.clearStorage()){
                            model = Model.getInstance(getActivity());
                            model.deleteAllpagesDownloaded();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(),"Capítulos excluidos com sucesso", Toast.LENGTH_LONG).show();
                                }
                            });
                            return;
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(),"Erro ao excluir os capítulos", Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                }.start();
            }
        });
        binding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Operação cancelada", Toast.LENGTH_LONG).show();
                binding.confirmDelete.setVisibility(View.GONE);
            }
        });
        startUpRadioGroup();
//        startUpRadioGroupImageOptions();
        return binding.getRoot();
    }
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.controllShowBottomNavigator(this);
            mainActivity.isInFirstDestination = false;
            mainActivity.isInReadFragment = false;
        }
    }

    private void startUpRadioGroup(){
        binding.radio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (binding.leftToRight.isChecked()) {
                    preferencesEditor.putInt(ConfigClass.ConfigReader.READ_DIRECTION, 1);
                    preferencesEditor.apply();

                    return;
                }
                if (binding.cascade.isChecked()) {
                    preferencesEditor.putInt(ConfigClass.ConfigReader.READ_DIRECTION, 3);
                    preferencesEditor.apply();
                    return;
                }
                preferencesEditor.putInt(ConfigClass.ConfigReader.READ_DIRECTION, 2);
                preferencesEditor.apply();
            }
        });

    }
    private void startUpRadioGroupImageOptions(){
//        binding.radio2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                if(binding.high.isChecked()){
//                    preferencesEditor.putString(ConfigClass.ConfigContent.IMAGE_QUALITY, MangaDexExtension.HIGH_QUALITY);
//                    preferencesEditor.apply();
//                    return;
//                }
//                preferencesEditor.putString(ConfigClass.ConfigContent.IMAGE_QUALITY,MangaDexExtension.LOW_QUALITY);
//                preferencesEditor.apply();
//            }
//        });
    }
}