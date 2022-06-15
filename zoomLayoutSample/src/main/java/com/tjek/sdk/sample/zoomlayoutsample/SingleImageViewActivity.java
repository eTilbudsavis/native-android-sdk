package com.tjek.sdk.sample.zoomlayoutsample;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tjek.sdk.publicationviewer.paged.zoomlayout.ZoomLayout;
import com.tjek.sdk.publicationviewer.paged.zoomlayout.ZoomOnDoubleTapListener;
import com.tjek.sdk.sample.zoomlayoutsample.utils.SimpleEventLogger;


public class SingleImageViewActivity extends AppCompatActivity {

    public static final String TAG = SingleImageViewActivity.class.getSimpleName();
    
    ZoomLayout mZoomLayout;
    TextView mTextView;
    ImageView mImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_single_imageview);

        mZoomLayout = findViewById(R.id.zoomLayout);
        mTextView = findViewById(R.id.info);
        mImageView = findViewById(R.id.imageViewSingle);

        // setup ZoomLayout
        SimpleEventLogger log = new SimpleEventLogger(TAG, mTextView);
        log.setLogger(mZoomLayout);

        mZoomLayout.setMinScale(1f);
        mZoomLayout.setMaxScale(4f);
        mZoomLayout.addZoomLayoutEventListener(new ZoomOnDoubleTapListener(false));

    }

}
