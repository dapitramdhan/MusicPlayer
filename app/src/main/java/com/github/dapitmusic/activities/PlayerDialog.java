package com.github.dapitmusic.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import android.widget.Toast;
import androidx.annotation.NonNull;

import androidx.core.content.ContextCompat;
import com.github.dapitmusic.R;
import com.github.dapitmusic.helper.MusicLibraryHelper;
import com.github.dapitmusic.listener.PlayerDialogListener;
import com.github.dapitmusic.model.Music;
import com.github.dapitmusic.player.PlayerListener;
import com.github.dapitmusic.player.PlayerManager;
import com.github.dapitmusic.player.PlayerQueue;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Locale;

// class BottomSheet

public class PlayerDialog extends BottomSheetDialog
		implements SeekBar.OnSeekBarChangeListener, PlayerListener, View.OnClickListener {

	private final PlayerManager playerManager;
	private final PlayerDialogListener playerDialogListener;
	private final PlayerQueue playerQueue;

	private final ImageView albumArt;
	private final ImageButton repeatControl;
	private final ImageButton shuffleControl;
	private final ImageButton prevControl;
	private final ImageButton nextControl;
	private final ImageButton playPauseControl;
	private final ImageButton musicQueue;
	private final ImageButton sleepTimer;
	private final TextView songName;
	private final TextView songAlbum;
	private final TextView currentDuration;
	private final TextView totalDuration;
	private final SeekBar songProgress;

	private Boolean dragging = false;
	private ImageButton btnClose;
	private ImageButton mEqualizer;

	// color 131418

	public PlayerDialog(@NonNull Context context, PlayerManager playerManager, PlayerDialogListener listener) {
		super(context);
		DisplayMetrics displayMetrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		BottomSheetBehavior<FrameLayout> behavior = getBehavior();
		behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
		behavior.setDraggable(false);
		View bottomSheet = getLayoutInflater().inflate(R.layout.preview_music, null);
		bottomSheet.setLayoutParams(
				new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, displayMetrics.heightPixels));
		setContentView(bottomSheet);

		this.playerDialogListener = listener;
		this.playerManager = playerManager;
		this.playerManager.attachListener(this);
		playerQueue = playerManager.getPlayerQueue();

		albumArt = findViewById(R.id.album_art);
		repeatControl = findViewById(R.id.control_repeat);
		shuffleControl = findViewById(R.id.control_shuffle);
		prevControl = findViewById(R.id.control_prev);
		nextControl = findViewById(R.id.control_next);
		playPauseControl = findViewById(R.id.control_play_pause);
		songName = findViewById(R.id.song_name);
		songAlbum = findViewById(R.id.song_album);
		currentDuration = findViewById(R.id.current_duration);
		totalDuration = findViewById(R.id.total_duration);
		songProgress = findViewById(R.id.song_progress);
		musicQueue = findViewById(R.id.music_queue);
		sleepTimer = findViewById(R.id.sleep_timer);
		btnClose = findViewById(R.id.btn_close);
		mEqualizer = findViewById(R.id.equalizer);

		btnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (behavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
					behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
				} else {
					behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
				}
			}
		});

		setUpUi();
		setUpListeners();

		this.setOnCancelListener(dialogInterface -> detachListener());
		this.setOnDismissListener(dialogInterface -> detachListener());
		
		if (Build.VERSION.SDK_INT >= 21) {

			Window window = getWindow();

			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

			window.setStatusBarColor(ContextCompat.getColor(context,R.color.amber));

		}
	}

	private void detachListener() {
		playerManager.detachListener(this);
	}

	private void setUpListeners() {
		songProgress.setOnSeekBarChangeListener(this);
		repeatControl.setOnClickListener(this);
		prevControl.setOnClickListener(this);
		playPauseControl.setOnClickListener(this);
		nextControl.setOnClickListener(this);
		shuffleControl.setOnClickListener(this);
		musicQueue.setOnClickListener(this);
		sleepTimer.setOnClickListener(this);
		mEqualizer.setOnClickListener(this);

		currentDuration.setText(getContext().getString(R.string.zero_time));
	}

	private void setUpUi() {
		Music music = playerManager.getCurrentMusic();
		songName.setSelected(true);
		songName.setText(music.title);
		songAlbum.setText(String.format(Locale.getDefault(), "%s • %s", music.artist, music.album));

		Glide.with(getContext().getApplicationContext()).load(music.albumArt)
				.placeholder(R.drawable.ic_notif_music_note).into(albumArt);

		int icon = playerManager.isPlaying() ? R.drawable.ic_controls_pause : R.drawable.ic_controls_play;
		playPauseControl.setImageResource(icon);

		if (playerQueue.isShuffle())
			shuffleControl.setAlpha(1f);
		else
			shuffleControl.setAlpha(0.3f);

		int repeat = playerQueue.isRepeat() ? R.drawable.ic_controls_repeat_one : R.drawable.ic_controls_repeat;
		repeatControl.setImageResource(repeat);

		totalDuration.setText(MusicLibraryHelper.formatDurationTimeStyle(playerManager.getDuration()));

		if (playerManager.getCurrentPosition() < 100)
			currentDuration.setText(
					MusicLibraryHelper.formatDurationTimeStyle(percentToPosition(playerManager.getCurrentPosition())));

	}

	private int percentToPosition(int percent) {
		return (playerManager.getDuration() * percent) / 100;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		currentDuration.setText(MusicLibraryHelper.formatDurationTimeStyle(percentToPosition(progress)));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		dragging = true;
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		playerManager.seekTo(percentToPosition(seekBar.getProgress()));
		dragging = false;
	}

	@Override
	public void onPrepared() {

	}

	@Override
	public void onStateChanged(int state) {
		if (state == State.PLAYING)
			playPauseControl.setImageResource(R.drawable.ic_controls_pause);
		else
			playPauseControl.setImageResource(R.drawable.ic_controls_play);
	}

	@Override
	public void onPositionChanged(int position) {
		if (!dragging)
			songProgress.setProgress(position);
	}

	@Override
	public void onMusicSet(Music music) {
		setUpUi();
	}

	@Override
	public void onPlaybackCompleted() {
	}

	@Override
	public void onRelease() {
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.control_repeat)
			setRepeat();
		else if (id == R.id.control_shuffle)
			setShuffle();
		else if (id == R.id.control_prev)
			playerManager.playPrev();
		else if (id == R.id.control_next)
			playerManager.playNext();
		else if (id == R.id.control_play_pause)
			playerManager.playPause();
		else if (id == R.id.music_queue)
			this.playerDialogListener.queueOptionSelect();
		else if (id == R.id.sleep_timer)
			this.playerDialogListener.sleepTimerOptionSelect();
		else if (id == R.id.equalizer)
			this.playerDialogListener.showEqualizerSettings();
	}

	private void setRepeat() {
		playerQueue.setRepeat((!playerQueue.isRepeat()));
		Toast.makeText(getContext(), "Repeat", Toast.LENGTH_SHORT).show();
	}

	private void setShuffle() {
		playerQueue.setShuffle((!playerQueue.isShuffle()));
		Toast.makeText(getContext(), "Shuffle", Toast.LENGTH_SHORT).show();
	}

}
