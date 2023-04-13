package com.github.dapitmusic.activities;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.dapitmusic.R;
import com.github.dapitmusic.activities.queue.QueueItemCallback;
import com.github.dapitmusic.adapter.QueueAdapter;
import com.github.dapitmusic.model.Music;
import com.github.dapitmusic.player.PlayerQueue;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class QueueDialog extends BottomSheetDialog {

    public QueueDialog(@NonNull Context context, PlayerQueue queue) {
        super(context);
        setContentView(R.layout.dialog_queue);

        List<Music> musicList = new ArrayList<>(queue.getCurrentQueue());

        RecyclerView queueLayout = findViewById(R.id.queue_layout);
        assert queueLayout != null;

        queueLayout.setLayoutManager(new LinearLayoutManager(getContext()));
        QueueAdapter queueAdapter = new QueueAdapter(context, musicList, queue);
        ItemTouchHelper.Callback callback = new QueueItemCallback(queueAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(queueLayout);

        queueLayout.setAdapter(queueAdapter);
        queueLayout.scrollToPosition(queue.getCurrentPosition());
    }
}
