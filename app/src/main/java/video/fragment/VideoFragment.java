package video.fragment;

import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.recyclerview.widget.RecyclerView;
import com.github.dapitmusic.R;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import androidx.fragment.app.Fragment;
import java.io.File;
import java.util.ArrayList;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class VideoFragment extends Fragment {

	private View view;
	private ProgressBar progressBar;
	public static ImageView ivnodata;

	public static RecyclerView recyclerVideo;

	@Override
	public void onDestroy() {
		super.onDestroy();
		org.greenrobot.eventbus.EventBus.getDefault().unregister(this);
	}

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		org.greenrobot.eventbus.EventBus.getDefault().register(this);
	}

	@Override
	public View onCreateView(LayoutInflater arg0, ViewGroup arg1, Bundle arg2) {
		view = arg0.inflate(R.layout.fragment_video, arg1, false);

		new Thread(new Runnable() {
			public void run() {
				VideoFragment.this.getVideo();
			}
		});

		initView();
		return view;
	}

	private void initView() {
		progressBar = view.findViewById(R.id.progress);
	}

	public void getVideo() {
		ArrayList arrayList = new ArrayList<>();
		final ArrayList arrayList2 = new ArrayList<>();
		arrayList.clear();
		arrayList2.clear();

		char c = 0;
		char c1 = 2;
		char c2 = 3;
		char c3 = 4;
		int i = 4;

		String[] strArr = { "_data", "title", "date_modified", "bucket_display_name", "_size", "date_added", "duration",
				"resolution" };
		Cursor query = getActivity().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, strArr,
				null, null, "datetaken DESC");

	}
}