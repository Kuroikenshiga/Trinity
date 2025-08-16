package com.example.trinity.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.trinity.R;
import com.example.trinity.adapters.MangaDexTagsAdapter;
import com.example.trinity.databinding.TagShowMangadexDialogLayoutBinding;
import com.example.trinity.valueObject.TagManga;
import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.Objects;

public class TagShowMangaDexDialog extends DialogFragment {

    private ArrayList<TagManga> elements;
    private ArrayList<String> selectedTags;
    private Runnable runnable;

    public TagShowMangaDexDialog(@NonNull ArrayList<TagManga> elements, @NonNull ArrayList<String> selectedTags) {
        this.elements = elements;
        this.selectedTags = selectedTags;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TagShowMangadexDialogLayoutBinding binding = TagShowMangadexDialogLayoutBinding.inflate(inflater,container,false);

        binding.tags.setAdapter(new MangaDexTagsAdapter(requireContext(),elements,selectedTags));
        binding.tags.setLayoutManager(new LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false));

        binding.actionBtn.setOnClickListener((v)->{
            this.dismiss();
        });
        return binding.getRoot();
    }
    public void setOnDismisslistener(Runnable runnable){
        this.runnable = runnable;
    }

    @Override
    public void onStart() {
        super.onStart();
        Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).setBackgroundDrawableResource(R.drawable.dialog_shape);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        runnable.run();
    }
}
