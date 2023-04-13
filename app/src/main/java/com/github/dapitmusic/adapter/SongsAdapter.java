package com.github.dapitmusic.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.dapitmusic.MPConstants;
import com.github.dapitmusic.MPPreferences;
import com.github.dapitmusic.R;
import com.github.dapitmusic.adapter.QueueAdapter.MyViewHolder;
import com.github.dapitmusic.equalizer.animation.EqualizerAnimation;
import com.github.dapitmusic.helper.MusicLibraryHelper;
import com.github.dapitmusic.listener.MusicSelectListener;
import com.github.dapitmusic.model.Music;
import com.bumptech.glide.Glide;

import com.github.dapitmusic.player.PlayerManager;
import com.github.dapitmusic.player.PlayerQueue;
import java.util.List;
import java.util.Locale;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.MyViewHolder> {

	private final List<Music> musicList;
	public MusicSelectListener listener;
	public EqualizerAnimation equalizerAnimation;
	int currentMusic = -1;

	public SongsAdapter(MusicSelectListener listener, List<Music> musics) {
		this.listener = listener;
		this.musicList = musics;
	}

	@NonNull
	@Override
	public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_songs, parent, false);
		return new MyViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
		Music music = musicList.get(position);
		holder.songName.setText(music.title);
		holder.albumName.setText(String.format(Locale.getDefault(), "%s - %s", music.artist, music.album));

		
		if (holder.state && !music.albumArt.equals("")) {
			Glide.with(holder.albumArt.getContext()).load(music.albumArt).placeholder(R.drawable.ic_notif_music_note)
					.into(holder.albumArt);
		} else if (music.albumArt.equals("")) {
			holder.albumArt.setImageResource(R.drawable.ic_notif_music_note);
		} 
	}

	@Override
	public int getItemCount() {
		return musicList.size();
	}

	public class MyViewHolder extends RecyclerView.ViewHolder {

		private final TextView songName;
		private final TextView albumName;
		private final ImageView albumArt;
		private final boolean state;

		private EqualizerAnimation equalizerAnimation;

		public MyViewHolder(@NonNull View itemView) {
			super(itemView);

			state = MPPreferences.getAlbumRequest(itemView.getContext());
			albumArt = itemView.findViewById(R.id.album_art);
			songName = itemView.findViewById(R.id.song_name);
			albumName = itemView.findViewById(R.id.song_album);
			equalizerAnimation = itemView.findViewById(R.id.equalizer_view);
			equalizerAnimation.setVisibility(View.GONE);

			itemView.findViewById(R.id.root_layout).setOnClickListener(
					v -> listener.playQueue(musicList.subList(getAdapterPosition(), musicList.size()), false));
		}
	}

}
