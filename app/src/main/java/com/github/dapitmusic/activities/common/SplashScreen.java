package com.github.dapitmusic.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.github.dapitmusic.MainActivity;
import com.github.dapitmusic.R;

public class SplashScreen extends AppCompatActivity{
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.splash_screen);
		showSplash();
	}
	
	private void showSplash(){
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run(){
				Intent splash = new Intent(SplashScreen.this, MainActivity.class);
				startActivity(splash);
				overridePendingTransition(R.anim.fade,R.anim.leave);
				finish();
			}
		}, 1800);
	}
}