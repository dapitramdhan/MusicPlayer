package com.github.dapitmusic.fragments;

import android.os.Bundle;
import com.github.dapitmusic.R;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import androidx.fragment.app.Fragment;

public class FragmentEmpty extends Fragment{
	@Override
	public View onCreateView(LayoutInflater arg0, ViewGroup arg1, Bundle arg2) {
		return arg0.inflate(R.layout.tampilan_setting, arg1,false);
	}
}