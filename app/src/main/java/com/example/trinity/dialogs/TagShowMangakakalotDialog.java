package com.example.trinity.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.trinity.adapters.MangakakalotTagsAdapter;
import com.example.trinity.databinding.TagShowMagakakalotDialogLayoutBinding;
import com.example.trinity.valueObject.TagManga;

import java.util.ArrayList;
import java.util.Objects;

public class TagShowMangakakalotDialog extends DialogFragment {

    TagShowMagakakalotDialogLayoutBinding binding;
    private ArrayList<TagManga> tags;
    private Runnable runnableDismiss;
    public String genre;
    public TagShowMangakakalotDialog(@NonNull ArrayList<TagManga> tags) {
        this.tags = tags;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.binding = TagShowMagakakalotDialogLayoutBinding.inflate(inflater,container,false);
        binding.tags.setAdapter(new MangakakalotTagsAdapter(requireContext(),tags, this::dismiss));
        binding.tags.setLayoutManager(new GridLayoutManager(requireContext(),2));

        return this.binding.getRoot();
    }
    public void setOnDismissListener(Runnable runnable){
        this.runnableDismiss = runnable;
    }
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        this.genre = ((MangakakalotTagsAdapter) Objects.requireNonNull(binding.tags.getAdapter())).getTagName().replace(" ","-").toLowerCase();
        super.onDismiss(dialog);
        if(this.runnableDismiss != null)this.runnableDismiss.run();
    }
}
