package com.example.trinity.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.trinity.fragments.ExtensionsShowFragment;
import com.example.trinity.fragments.HistoryMangaFragment;
import com.example.trinity.fragments.LibraryFragment;
import com.example.trinity.fragments.SettingsFragment;
import com.example.trinity.fragments.UpdatesFragment;

public class AdapterNavigation extends FragmentStateAdapter {

    private FragmentActivity activity;
    private final Fragment[] routes = {new LibraryFragment(),new UpdatesFragment(),new ExtensionsShowFragment(),new HistoryMangaFragment(),new SettingsFragment()};

    public AdapterNavigation(@NonNull FragmentActivity activity) {
        super(activity);
        this.activity = activity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return this.routes[position];
    }

    @Override
    public int getItemCount() {
        return this.routes.length;
    }
}
