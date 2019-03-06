package com.shopgun.android.sdk.demo;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.UUID;

import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FragmentLifecycleActivity extends AppCompatActivity {

    public static final String TAG = FragmentLifecycleActivity.class.getSimpleName();

    @BindView(R.id.fragmentOne) FrameLayout mOne;
    @BindView(R.id.fragmentTwo) FrameLayout mTwo;
    @BindView(R.id.fragmentThree) FrameLayout mThree;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_lifecycle);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        FrameworkFragment frameworkFragment = (FrameworkFragment) getFragmentManager().findFragmentByTag(FrameworkFragment.class.getSimpleName());
        if (frameworkFragment == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragmentOne, new FrameworkFragment(), FrameworkFragment.class.getSimpleName())
                    .commit();
        }

        SupportFragment supportFragment = (SupportFragment) getSupportFragmentManager().findFragmentByTag(SupportFragment.class.getSimpleName());
        if (supportFragment == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentThree,new SupportFragment(), SupportFragment.class.getSimpleName())
                    .commit();
        }

    }

    public static class SupportFragment extends Fragment {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            TextView tv = new TextView(container.getContext());
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tv.setLayoutParams(lp);
            tv.setText("Support Fragment (" + getFragmentManager().getBackStackEntryCount() + ")");
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.fragmentThree,new SupportFragment(), SupportFragment.class.getSimpleName())
                            .addToBackStack(UUID.randomUUID().toString())
                            .commit();
                }
            });
            return tv;
        }
    }

    public static class FrameworkFragment extends android.app.Fragment {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            TextView tv = new TextView(container.getContext());
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tv.setLayoutParams(lp);
            tv.setText("Framework Fragment (" + getFragmentManager().getBackStackEntryCount() + ")");
            return tv;
        }
    }

}
