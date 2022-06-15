package com.tjek.sdk.sample.zoomlayoutsample;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tjek.sdk.publicationviewer.paged.zoomlayout.ZoomLayout;
import com.tjek.sdk.sample.zoomlayoutsample.utils.SimpleEventLogger;

public class MultipleViewActivity extends AppCompatActivity {

    public static final String TAG = MultipleViewActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_view);

        ZoomLayout zoomLayout = (ZoomLayout) findViewById(R.id.zoomLayout);
        TextView textView = (TextView) findViewById(R.id.info);
        SimpleEventLogger log = new SimpleEventLogger(TAG, textView);
        log.setLogger(zoomLayout);

    }
}
