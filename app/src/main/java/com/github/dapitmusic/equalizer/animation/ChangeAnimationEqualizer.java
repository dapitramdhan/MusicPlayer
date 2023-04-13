package com.github.dapitmusic.equalizer.animation;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import com.github.dapitmusic.R;

public class ChangeAnimationEqualizer extends AppCompatActivity {

	Context context;
	EqualizerAnimation equalizerAnimation;

	public ChangeAnimationEqualizer(Context context) {
		this.context = context;
		setContentView(R.layout.view_equalizer_animation);
		showEqualizer();
	}

	private void toggleEqualizer() {
		if (equalizerAnimation.isAnimating()) {
			equalizerAnimation.stopBars();
		} else {
			equalizerAnimation.animateBars();
		}
	}

	public void showEqualizer() {
		equalizerAnimation.animateBars();
	}
}