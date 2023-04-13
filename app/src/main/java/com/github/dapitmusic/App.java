package com.github.dapitmusic;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;

public class App extends Application {
	@SuppressLint("StaticFieldLeak")
	private static Context context;

	public static Context getContext() {
		return App.context;
	}

	public void onCreate() {
		super.onCreate();
		App.context = getApplicationContext();
	}
}