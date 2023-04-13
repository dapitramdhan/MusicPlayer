package com.github.dapitmusic.equalizer;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.github.dapitmusic.MPConstants;
import com.github.dapitmusic.R;
import com.github.dapitmusic.listener.MusicSelectListener;
import com.github.dapitmusic.model.Music;
import com.github.dapitmusic.player.PlayerBuilder;
import com.github.dapitmusic.player.PlayerListener;
import com.google.gson.Gson;
import java.util.Collections;
import java.util.List;
import com.github.dapitmusic.player.PlayerManager;

public class EqualizerActivity extends AppCompatActivity implements PlayerListener {

	private PlayerManager playerManager;
	private PlayerBuilder playerBuilder;
	private MediaPlayer mediaPlayer;

	private List<Music> listMusic;
	
	int sessionId;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_equalizer);
		
		EqualizerFragment fragment = EqualizerFragment.newBuilder().setAudioSessionId(sessionId).build();
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_equalizer, fragment).commit();
		loadEqualizerSettings();
		setEqualizerView();
		setupUiElement();
	}
	
	private void setEqualizerView(){
		if (playerManager != null && playerManager.isPlaying()){
			onMusicSet(playerManager.getCurrentMusic());
		}
	}
	
	private void setupUiElement(){
		playerBuilder = new PlayerBuilder(EqualizerActivity.this,this);
	}
	
	@Override
	public void onPrepared() {
		playerManager = playerBuilder.getPlayerManager();
		setEqualizerView();
	}

	@Override
	public void onStateChanged(int state) {
	}

	@Override
	public void onPositionChanged(int position) {
	}

	@Override
	public void onMusicSet(Music music) {
		if (playerManager != null && playerManager.isPlaying()) {
			sessionId = playerManager.getAudioSessionId();
		}
	}

	@Override
	public void onPlaybackCompleted() {
	}

	@Override
	public void onRelease() {
	}

	@Override
	protected void onStop() {
		super.onStop();
		saveEqualizerSettings();
	}

	@Override
	protected void onPause() {
		try {
			//mediaPlayer.pause();
		} catch (Exception e) {

		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					//playerManager.isPlay();
				}
			}, 2000);
		} catch (Exception ex) {
			//ignore
		}
	}

	private void saveEqualizerSettings() {
		if (Settings.equalizerModel != null) {
			EqualizerSettings settings = new EqualizerSettings();
			settings.bassStrength = Settings.equalizerModel.getBassStrength();
			settings.presetPos = Settings.equalizerModel.getPresetPos();
			settings.reverbPreset = Settings.equalizerModel.getReverbPreset();
			settings.seekbarpos = Settings.equalizerModel.getSeekbarpos();

			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

			Gson gson = new Gson();
			preferences.edit().putString(PREF_KEY, gson.toJson(settings)).apply();
		}
	}

	private void loadEqualizerSettings() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

		Gson gson = new Gson();
		EqualizerSettings settings = gson.fromJson(preferences.getString(PREF_KEY, "{}"), EqualizerSettings.class);
		EqualizerModel model = new EqualizerModel();
		model.setBassStrength(settings.bassStrength);
		model.setPresetPos(settings.presetPos);
		model.setReverbPreset(settings.reverbPreset);
		model.setSeekbarpos(settings.seekbarpos);

		Settings.isEqualizerEnabled = true;
		Settings.isEqualizerReloaded = true;
		Settings.bassStrength = settings.bassStrength;
		Settings.presetPos = settings.presetPos;
		Settings.reverbPreset = settings.reverbPreset;
		Settings.seekbarpos = settings.seekbarpos;
		Settings.equalizerModel = model;
	}

	public static final String PREF_KEY = "equalizer";

}