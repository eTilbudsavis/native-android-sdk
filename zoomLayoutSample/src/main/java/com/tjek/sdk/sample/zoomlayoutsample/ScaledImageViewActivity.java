package com.tjek.sdk.sample.zoomlayoutsample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tjek.sdk.publicationviewer.paged.zoomlayout.ZoomLayout;
import com.tjek.sdk.publicationviewer.paged.zoomlayout.ZoomOnDoubleTapListener;
import com.tjek.sdk.sample.zoomlayoutsample.utils.SimpleEventLogger;


public class ScaledImageViewActivity extends AppCompatActivity {

    public static final String TAG = ScaledImageViewActivity.class.getSimpleName();
    
    ZoomLayout mZoomLayout;
    TextView mTextView;
    ScaledImageView mImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scaled_imageview);

        mZoomLayout = (ZoomLayout) findViewById(R.id.zoomLayout);
        mTextView = (TextView) findViewById(R.id.info);
        mImageView = (ScaledImageView) findViewById(R.id.imageViewScaled);

        loadImageFromBitmap();

        // setup ZoomLayout
        SimpleEventLogger log = new SimpleEventLogger(TAG, mTextView);
        log.setLogger(mZoomLayout);

        mZoomLayout.setMinScale(1f);
        mZoomLayout.setMaxScale(4f);
        mZoomLayout.addEventListener(new ZoomOnDoubleTapListener(false));

    }

    private void loadImageFromBitmap() {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.boat, options);
        float w = (float) b.getWidth();
        float h = (float) b.getHeight();
        float aspectRatio = w/h;
        mImageView.setImageAspectRatio(aspectRatio);
        mImageView.setImageBitmap(b);

    }

}
