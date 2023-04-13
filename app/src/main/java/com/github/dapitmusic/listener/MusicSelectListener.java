package com.github.dapitmusic.listener;

import com.github.dapitmusic.model.Music;

import java.util.List;

public interface MusicSelectListener {
	
    void playQueue(List<Music> musicList, boolean shuffle);

    void addToQueue(List<Music> musicList);

    void refreshMediaLibrary();
	
	void changeEqualizerView();
}