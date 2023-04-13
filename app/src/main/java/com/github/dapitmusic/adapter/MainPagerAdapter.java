package com.github.dapitmusic.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.github.dapitmusic.fragments.AlbumsFragment;
import com.github.dapitmusic.fragments.ArtistsFragment;
import com.github.dapitmusic.fragments.SettingsFragment;
import com.github.dapitmusic.fragments.SongsFragment;
import com.github.dapitmusic.listener.MusicSelectListener;

import java.util.ArrayList;
import java.util.List;

public class MainPagerAdapter extends FragmentPagerAdapter {

    private final MusicSelectListener selectListener;
    List<Fragment> fragments = new ArrayList<>();
	private String[] tabTitle = new String[]{"Semua lagu","Daftar Putar","Artist","Albums","Folder"};

    public MainPagerAdapter(FragmentManager fm, MusicSelectListener selectListener) {
        super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.selectListener = selectListener;

        setFragments();
    }

    public void setFragments() {
        fragments.add(SongsFragment.newInstance(selectListener));
        fragments.add(ArtistsFragment.newInstance());
        fragments.add(AlbumsFragment.newInstance());
       // fragments.add(SettingsFragment.newInstance());
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitle[position];
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}