package com.example.trinity.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.trinity.R;
import com.example.trinity.databinding.SetGrayScaleLayoutDialogBinding;

public class SetGrayScaleDialog extends DialogFragment {
    private SetGrayScaleLayoutDialogBinding binding;

    private Runnable percent60,percent40,percent85;

    public SetGrayScaleDialog(@NonNull Runnable percent60,@NonNull Runnable percent40,@NonNull Runnable percent85) {
        this.percent60 = percent60;
        this.percent40 = percent40;
        this.percent85 = percent85;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SetGrayScaleLayoutDialogBinding.inflate(inflater,container,false);

        binding.percent60.setOnClickListener((v)->{percent60.run();this.dismiss();});
        binding.percent40.setOnClickListener((v)->{percent40.run();this.dismiss();});
        binding.percent85.setOnClickListener((v)->{percent85.run();this.dismiss();});


        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialog_shape);
    }
}
