package com.github.dapitmusic.fragments;

import android.os.Bundle;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.github.dapitmusic.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;

public class VideoFragment extends Fragment {

	View mRoot;

	@Override
	public View onCreateView(@NonNull LayoutInflater arg0, @Nullable ViewGroup arg1, @Nullable Bundle arg2) {
		mRoot = arg0.inflate(R.layout.fragment_video, arg1, false);

		return mRoot;
	}

	@Override
	public void onViewCreated(View arg0, Bundle arg1) {
		super.onViewCreated(arg0, arg1);

	}

}