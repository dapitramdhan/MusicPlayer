package com.github.dapitmusic.fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.TextView;
import com.github.dapitmusic.AppConfig;
import com.github.dapitmusic.equalizer.EqualizerActivity;
import com.github.dapitmusic.player.PlayerManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.dapitmusic.MPConstants;
import com.github.dapitmusic.MPPreferences;
import com.github.dapitmusic.R;

import com.github.dapitmusic.activities.FolderDialog;
import com.github.dapitmusic.adapter.AccentAdapter;
import com.github.dapitmusic.helper.ThemeHelper;
import com.github.dapitmusic.model.Folder;
import com.github.dapitmusic.viewmodel.MainViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment implements View.OnClickListener {

	private View mRoot;
	private MainViewModel viewModel;
	private RecyclerView accentView;
	private boolean state;
	private LinearLayout chipLayout;
	private ImageView currentThemeMode;
	private TextView packageName, versionName;

	private List<Folder> folderList;
	private MaterialToolbar toolbar;
	private FolderDialog folderDialog;
	private EqualizerActivity equalizerDialog;

	public static final String PREF_KEY = "equalizer";

	public SettingsFragment() {
	}

	public static SettingsFragment newInstance() {
		return new SettingsFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		mRoot = inflater.inflate(R.layout.fragment_settings, container, false);

		viewModel.getFolderList().observe(requireActivity(), folders -> {
			if (folderList == null)
				folderList = new ArrayList<>();
			folderList.clear();
			folderList.addAll(folders);
		});

		SwitchMaterial switchMaterial = mRoot.findViewById(R.id.album_switch);
		accentView = mRoot.findViewById(R.id.accent_view);
		chipLayout = mRoot.findViewById(R.id.chip_layout);
		currentThemeMode = mRoot.findViewById(R.id.current_theme_mode);
		toolbar = mRoot.findViewById(R.id.toolbar);
		packageName = mRoot.findViewById(R.id.package_name);
		versionName = mRoot.findViewById(R.id.version_name);

		LinearLayout accentOption = mRoot.findViewById(R.id.accent_option);
		LinearLayout albumOption = mRoot.findViewById(R.id.album_options);
		LinearLayout themeModeOption = mRoot.findViewById(R.id.theme_mode_option);
		LinearLayout folderOption = mRoot.findViewById(R.id.folder_options);
		LinearLayout refreshOption = mRoot.findViewById(R.id.refresh_options);
		LinearLayout equalizer = mRoot.findViewById(R.id.equalizer_layout);

		state = MPPreferences.getAlbumRequest(requireActivity().getApplicationContext());
		switchMaterial.setChecked(state);
		setCurrentThemeMode();

		accentView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
		accentView.setAdapter(new AccentAdapter(getActivity()));

		accentOption.setOnClickListener(this);
		albumOption.setOnClickListener(this);
		switchMaterial.setOnClickListener(this);
		themeModeOption.setOnClickListener(this);
		folderOption.setOnClickListener(this);
		refreshOption.setOnClickListener(this);
		//equalizer.setOnClickListener(this);

		
		mRoot.findViewById(R.id.night_chip).setOnClickListener(this);
		mRoot.findViewById(R.id.light_chip).setOnClickListener(this);
		mRoot.findViewById(R.id.auto_chip).setOnClickListener(this);
		mRoot.findViewById(R.id.review_options).setOnClickListener(this);

		setUpOptions();
		versionApp();
		return mRoot;

	}

	private void setUpOptions() {
		toolbar.setOnMenuItemClickListener(item -> {
			int id = item.getItemId();

			if (id == R.id.github) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MPConstants.GITHUB_REPO_URL)));
				return true;
			}

			return false;
		});
		toolbar.setNavigationOnClickListener(v -> requireActivity().finish());
	}

	private void setCurrentThemeMode() {
		int mode = MPPreferences.getThemeMode(requireActivity().getApplicationContext());

		if (mode == AppCompatDelegate.MODE_NIGHT_NO)
			currentThemeMode.setImageResource(R.drawable.ic_theme_mode_light);

		else if (mode == AppCompatDelegate.MODE_NIGHT_YES)
			currentThemeMode.setImageResource(R.drawable.ic_theme_mode_night);

		else
			currentThemeMode.setImageResource(R.drawable.ic_theme_mode_auto);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		if (id == R.id.accent_option) {
			int visibility = (accentView.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE;
			accentView.setVisibility(visibility);
		} else if (id == R.id.album_options)
			setAlbumRequest();

		else if (id == R.id.album_switch)
			setAlbumRequest();

		else if (id == R.id.theme_mode_option) {
			int mode = chipLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
			chipLayout.setVisibility(mode);
		} else if (id == R.id.night_chip)
			selectTheme(AppCompatDelegate.MODE_NIGHT_YES);

		else if (id == R.id.light_chip)
			selectTheme(AppCompatDelegate.MODE_NIGHT_NO);

		else if (id == R.id.auto_chip)
			selectTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

		else if (id == R.id.folder_options)
			showFolderSelectionDialog();

		else if (id == R.id.refresh_options) {
			refreshMediaLibrary();
		} else if (id == R.id.review_options) {
			setUpRateReview();
		} //else if (id == R.id.equalizer_layout){
			//	setUpEqualizer();
			//	}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (folderDialog != null)
			folderDialog.dismiss();
	}

	private void showFolderSelectionDialog() {
		if (folderList != null) {
			folderDialog = new FolderDialog(requireActivity(), folderList);
			folderDialog.show();

			folderDialog.setOnDismissListener(dialog -> refreshMediaLibrary());
		} else {
			Toast.makeText(requireActivity(), "Folder list missing", Toast.LENGTH_SHORT).show();
		}
	}

	private void refreshMediaLibrary() {
		showSnackbar(mRoot);
		//Snackbar.make(requireActivity(), "Refreshing media library", Snackbar.LENGTH_SHORT).show();
		MPConstants.musicSelectListener.refreshMediaLibrary();
	}

	private void selectTheme(int theme) {
		AppCompatDelegate.setDefaultNightMode(theme);
		MPPreferences.storeThemeMode(requireActivity().getApplicationContext(), theme);
	}

	private void setAlbumRequest() {
		MPPreferences.storeAlbumRequest(requireActivity().getApplicationContext(), (!state));
		ThemeHelper.applySettings(getActivity());
	}

	private void setUpRateReview() {
		startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(MPConstants.PLAY_STORE_LINK)));
	}

	private void versionApp() {
		String version = AppConfig.VERSION_NAME ;
		String name = "Version";
		packageName.setText(name);
		versionName.setText(version);
	}
	
	private void showSnackbar(View s1){
		Snackbar.make(s1,"Resresh Library", Snackbar.LENGTH_LONG).show();
	}

	private void setUpEqualizer() {
		//	Intent intent = new Intent(requireActivity(), EqualizerActivity.class);
		//	intent.putExtra(PREF_KEY, "equalizer");
		//	startActivity(intent);
	}

}