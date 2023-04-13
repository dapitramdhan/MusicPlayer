package com.github.dapitmusic;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
import com.bumptech.glide.Glide;

import com.github.dapitmusic.activities.PlayerDialog;
import com.github.dapitmusic.activities.QueueDialog;
import com.github.dapitmusic.activities.SearchActivity;
import com.github.dapitmusic.adapter.SongsAdapter;
import com.github.dapitmusic.dialogs.SleepTimerDialog;
import com.github.dapitmusic.dialogs.SleepTimerDisplayDialog;
import com.github.dapitmusic.equalizer.DialogEqualizerFragment;
import com.github.dapitmusic.equalizer.EqualizerActivity;
import com.github.dapitmusic.equalizer.EqualizerFragment;
import com.github.dapitmusic.equalizer.animation.EqualizerAnimation;
import com.github.dapitmusic.fragments.SettingsFragment;
import com.github.dapitmusic.helper.ListHelper;
import com.github.dapitmusic.helper.ThemeHelper;
import com.github.dapitmusic.adapter.MainPagerAdapter;
import com.github.dapitmusic.fragments.FragmentEmpty;
import com.github.dapitmusic.helper.MusicLibraryHelper;
import com.github.dapitmusic.listener.PlayerDialogListener;
import com.github.dapitmusic.listener.SleepTimerSetListener;
import com.github.dapitmusic.model.Music;
import com.github.dapitmusic.listener.MusicSelectListener;
import com.github.dapitmusic.player.PlayerBuilder;
import com.github.dapitmusic.player.PlayerListener;
import com.github.dapitmusic.player.PlayerManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.github.dapitmusic.viewmodel.MainViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.android.material.tabs.TabLayout;
import java.util.Locale;
import video.fragment.VideoFragment;

public class MainActivity extends AppCompatActivity implements MusicSelectListener, PlayerListener,
		View.OnClickListener, SleepTimerSetListener, PlayerDialogListener {

	public static boolean isSleepTimerRunning;
	public static MutableLiveData<Long> sleepTimerTick;
	public static CountDownTimer sleepTimer;

	private ViewPager viewPager;
	private TabLayout tabs;
	private BottomNavigationView bottomNavigationView;

	private FrameLayout frameLayout;
	private CoordinatorLayout childLayout;
	private RelativeLayout playerView;
	private ImageView albumArt;
	private TextView songName, songDetails;
	private ImageButton play_pause,imgMusic;
	private LinearProgressIndicator progressIndicator;
	private Toolbar toolbar;
	private SearchView searchView;
	private SwipeRefreshLayout swipeRefreshLayout;

	private PlayerDialog playerDialog;
	private QueueDialog queueDialog;

	private PlayerManager playerManager;
	private PlayerBuilder playerBuilder;

	private MainViewModel viewModel;

	private boolean albumState;
	private int sessionId;

	public static final String PREF_KEY = "equalizer";
	public static Context ctx;

	private EqualizerAnimation equalizerAnimation;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.gray_dark));
		setTheme(ThemeHelper.getTheme(MPPreferences.getTheme(MainActivity.this)));
		AppCompatDelegate.setDefaultNightMode(MPPreferences.getThemeMode(MainActivity.this));
		setContentView(R.layout.activity_main);
		MPConstants.musicSelectListener = this;
		viewModel = new ViewModelProvider(this).get(MainViewModel.class);

		frameLayout = (FrameLayout) findViewById(R.id.fragment_container);
		childLayout = (CoordinatorLayout) findViewById(R.id.child);

		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentEmpty()).commit();

		if (hasReadStoragePermission(MainActivity.this)) {
			fetchMusicList();
			setUpUiElements();
		} else {
			manageStoragePermission(MainActivity.this);
		}

		albumState = MPPreferences.getAlbumRequest(this);

		MaterialCardView playerLayout = findViewById(R.id.player_layout);
		playerView = findViewById(R.id.player_view);
		albumArt = findViewById(R.id.albumArt);
		progressIndicator = findViewById(R.id.song_progress);
		songName = findViewById(R.id.song_title);
		songDetails = findViewById(R.id.song_details);
		play_pause = findViewById(R.id.control_play_pause);
		ImageButton queue = findViewById(R.id.control_queue);
		toolbar = findViewById(R.id.toolbar);
		equalizerAnimation = findViewById(R.id.equalizer_view);
		imgMusic = findViewById(R.id.img_music);
		
		play_pause.setOnClickListener(this);
		playerLayout.setOnClickListener(this);
		queue.setOnClickListener(this);
		initBottomNavigation();

		setUpOptionsMenu();
	}

	//
	private void setPlayerView() {
		if (playerManager != null && playerManager.isPlaying()) {
			playerView.setVisibility(View.VISIBLE);
			onMusicSet(playerManager.getCurrentMusic());
		}
	}

	//Tampilkan Lagu
	private void fetchMusicList() {
		new Handler().post(() -> {
			List<Music> musicList = MusicLibraryHelper.fetchMusicLibrary(MainActivity.this);
			viewModel.setSongsList(musicList);
			viewModel.parseFolderList(musicList);
		});
	}

	private void setUpUiElements() {
		playerBuilder = new PlayerBuilder(MainActivity.this, this);
		MainPagerAdapter sectionsPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), this);

		viewPager = findViewById(R.id.view_pager);
		viewPager.setAdapter(sectionsPagerAdapter);
		viewPager.setOffscreenPageLimit(5);

		tabs = findViewById(R.id.tabs);
		tabs.setupWithViewPager(viewPager);
	}

	//
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (playerBuilder != null) {
			playerBuilder.unBindService();
		}
		if (playerDialog != null) {
			playerDialog.dismiss();
		}
		if (queueDialog != null) {
			queueDialog.dismiss();
		}
	}
	@Override
	protected void onResume(){
		super.onResume();
		equalizerAnimation.animateBars();
	}

	//Music Listener
	@Override
	public void playQueue(List<Music> musicList, boolean shuffle) {
		if (shuffle) {
			Collections.shuffle(musicList);
		}
		if (musicList.size() > 0) {
			playerManager.setMusicList(musicList);
			setPlayerView();
			//setUpPlayerDialog();
			changeEqualizerView();
		}
	}

	@Override
	public void addToQueue(List<Music> musicList) {
		if (musicList.size() > 0) {
			if (playerManager != null && playerManager.isPlaying()) {
				playerManager.addMusicQueue(musicList);
			} else if (playerManager != null)
				playerManager.setMusicList(musicList);
			setPlayerView();

		}
	}

	@Override
	public void refreshMediaLibrary() {
		fetchMusicList();
	}

	@Override
	public void changeEqualizerView() {
		//equalizerAnimation.setVisibility(View.VISIBLE);
		equalizerAnimation.animateBars();
	}

	//Player Listener
	@Override
	public void onPrepared() {
		playerManager = playerBuilder.getPlayerManager();
		setPlayerView();
	}

	@Override
	public void onStateChanged(int state) {
		if (state == State.PLAYING) {
			play_pause.setImageResource(R.drawable.ic_controls_pause);
			equalizerAnimation.animateBars();
			equalizerAnimation.setVisibility(View.VISIBLE);
			imgMusic.setVisibility(View.GONE);
		} else {
			play_pause.setImageResource(R.drawable.ic_controls_play);
			equalizerAnimation.stopBars();
			equalizerAnimation.setVisibility(View.GONE);
			imgMusic.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onPositionChanged(int position) {
		progressIndicator.setProgress(position);
	}

	@Override
	public void onMusicSet(Music music) {
		songName.setSelected(true);
		songName.setText(music.title);
		songDetails.setText(String.format(Locale.getDefault(), "%s - %s", music.artist, music.album));
		playerView.setVisibility(View.VISIBLE);

		if (albumState)
			Glide.with(getApplicationContext()).load(music.albumArt).centerCrop().into(albumArt);

		if (playerManager != null && playerManager.isPlaying())
			play_pause.setImageResource(R.drawable.ic_controls_pause);
		else
			play_pause.setImageResource(R.drawable.ic_controls_play);
	}

	@Override
	public void onPlaybackCompleted() {
	}

	@Override
	public void onRelease() {
		playerView.setVisibility(View.GONE);
	}

	//
	private void initBottomNavigation() {
		bottomNavigationView = findViewById(R.id.bottom_navigation);
		bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(MenuItem item) {
				Fragment select = null;
				switch (item.getItemId()) {
				case R.id.nav_home:
					frameLayout.setVisibility(View.GONE);
					childLayout.setVisibility(View.VISIBLE);
					select = new FragmentEmpty();
					break;
				case R.id.nav_video:
					frameLayout.setVisibility(View.VISIBLE);
					childLayout.setVisibility(View.GONE);
					select = new VideoFragment();
					break;
				case R.id.nav_settings:
					frameLayout.setVisibility(View.VISIBLE);
					childLayout.setVisibility(View.GONE);
					select = new SettingsFragment();
					break;
				}
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, select).commit();
				return true;
			}
		});
	}

	//Ijin akses Storage
	public void manageStoragePermission(Activity context) {
		if (!hasReadStoragePermission(context)) {
			// required a dialog?
			if (ActivityCompat.shouldShowRequestPermissionRationale(context,
					Manifest.permission.READ_EXTERNAL_STORAGE)) {
				new MaterialAlertDialogBuilder(context).setTitle("Requesting permission")
						.setMessage("Enable storage permission for accessing the media files.")
						.setPositiveButton("Accept", (dialog, which) -> askReadStoragePermission(context)).show();
			} else
				askReadStoragePermission(context);
		}
	}

	public boolean hasReadStoragePermission(Activity context) {
		return (ContextCompat.checkSelfPermission(context,
				Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
	}

	public void askReadStoragePermission(Activity context) {
		ActivityCompat.requestPermissions(context, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
				MPConstants.PERMISSION_READ_STORAGE);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
			@NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			ThemeHelper.applySettings(this);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		if (id == R.id.control_play_pause)
			playerManager.playPause();
		if (id == R.id.control_queue)
			setUpQueueDialogHeadless();
		else if (id == R.id.player_layout)
			setUpPlayerDialog();

	}

	private void setUpPlayerDialog() {
		playerDialog = new PlayerDialog(this, playerManager, this);
		playerDialog.show();
	}

	@Override
	public void setTimer(int minutes) {
		if (!isSleepTimerRunning) {
			isSleepTimerRunning = true;
			sleepTimer = new CountDownTimer(minutes * 60 * 1000L, 1000) {
				@Override
				public void onTick(long l) {
					if (sleepTimerTick == null)
						sleepTimerTick = new MutableLiveData<>();
					sleepTimerTick.postValue(l);
				}

				@Override
				public void onFinish() {
					isSleepTimerRunning = false;
					playerManager.pauseMediaPlayer();
				}
			}.start();
		}
	}

	@Override
	public void cancelTimer() {
		if (isSleepTimerRunning && sleepTimer != null) {
			isSleepTimerRunning = false;
			sleepTimer.cancel();
		}
	}

	@Override
	public MutableLiveData<Long> getTick() {
		return sleepTimerTick;
	}

	@Override
	public void queueOptionSelect() {
		setUpQueueDialog();
	}

	@Override
	public void sleepTimerOptionSelect() {
		setUpSleepTimerDialog();
	}

	@Override
	public void showEqualizerSettings() {
		startActivity(new Intent(MainActivity.this, EqualizerActivity.class));
		overridePendingTransition(R.anim.fade, R.anim.leave);
	}

	private void setUpQueueDialog() {
		queueDialog = new QueueDialog(MainActivity.this, playerManager.getPlayerQueue());
		queueDialog.setOnDismissListener(v -> {
			if (!this.isDestroyed()) {
				playerDialog.show();
			}
		});

		playerDialog.dismiss();
		queueDialog.show();
	}

	private void setUpQueueDialogHeadless() {
		queueDialog = new QueueDialog(MainActivity.this, playerManager.getPlayerQueue());
		queueDialog.show();
	}

	private void setUpSleepTimerDialog() {
		if (MainActivity.isSleepTimerRunning) {
			setUpSleepTimerDisplayDialog();
			return;
		}
		SleepTimerDialog sleepTimerDialog = new SleepTimerDialog(MainActivity.this, this);
		sleepTimerDialog.setOnDismissListener(v -> {
			if (!this.isDestroyed())
				playerDialog.show();
		});

		playerDialog.dismiss();
		sleepTimerDialog.show();
	}

	private void setUpSleepTimerDisplayDialog() {
		SleepTimerDisplayDialog sleepTimerDisplayDialog = new SleepTimerDisplayDialog(MainActivity.this, this);
		sleepTimerDisplayDialog.setOnDismissListener(v -> {
			if (!this.isDestroyed())
				playerDialog.show();
		});

		playerDialog.dismiss();
		sleepTimerDisplayDialog.show();
	}

	private void setUpOptionsMenu() {
		// Inflate the menu; this adds items to the action bar if it is present.
		toolbar.inflateMenu(R.menu.menu_search);
		toolbar.setOnMenuItemClickListener(item -> {
			int id = item.getItemId();
			if (id == R.id.search) {
				startActivity(new Intent(MainActivity.this, SearchActivity.class));
				return true;
			}
			return false;
		});
	}

}