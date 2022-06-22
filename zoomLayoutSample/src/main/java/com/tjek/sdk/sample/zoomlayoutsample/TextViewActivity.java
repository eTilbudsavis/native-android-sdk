package com.tjek.sdk.sample.zoomlayoutsample;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tjek.sdk.publicationviewer.paged.libs.zoomlayout.ZoomLayout;
import com.tjek.sdk.sample.zoomlayoutsample.utils.SimpleEventLogger;

public class TextViewActivity extends AppCompatActivity {

    public static final String TAG = TextViewActivity.class.getSimpleName();
    
    ZoomLayout mZoomLayout;
    TextView mTextView;
    TextView mInfo;
    boolean mHorizontal = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textview);

        mZoomLayout = (ZoomLayout) findViewById(R.id.zoomLayout);
        mInfo = (TextView) findViewById(R.id.info);
        mTextView = (TextView) findViewById(R.id.textView);

        // setup ZoomLayout
        SimpleEventLogger log = new SimpleEventLogger(TAG, mInfo);
        log.setLogger(mZoomLayout);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 1, Menu.NONE, "Change Layout");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            mHorizontal = !mHorizontal;
            String text = mHorizontal ? "Hello, ZoomLayout!" : "H\ne\nl\nl\no\n,\n \nZ\no\no\nm\nL\na\ny\no\nu\nt\n!";
            mTextView.setText(text);
        }
        return super.onOptionsItemSelected(item);
    }

}
