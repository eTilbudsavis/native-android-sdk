package com.shopgun.android.sdk.utils;

import android.app.Activity;
import android.app.FragmentManager;
import android.support.v4.app.FragmentActivity;

import com.shopgun.android.sdk.corekit.LifecycleManager;
import com.shopgun.android.sdk.log.SgnLog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LifecycleEventLogger extends LifecycleManager.SimpleCallback {
    
    public static final String TAG = LifecycleEventLogger.class.getSimpleName();

    Map<Activity, FragmentManagerProxy> mMap = new HashMap<>();

    @Override
    public void onStart(Activity activity) {
        FragmentManagerProxy proxy = new FragmentManagerProxy(activity);
        proxy.addOnBackStackChangedListener(new OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged(List<FragmentProxy> fragments) {
                StringBuilder sb = new StringBuilder();
                for (FragmentProxy fragment : fragments) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(fragment.getSimpleName());
                }
                SgnLog.d(TAG, "Fragments: " + sb.toString());
            }
        });
        mMap.put(activity, proxy);
    }

    @Override
    public void onStop(Activity activity) {
        mMap.get(activity).removeOnBackStackChangedListener();
    }

    @Override
    public void onDestroy(Activity activity) {
        // Maybe cleanup, but would be nice to have a full record of this
    }

    private static class FragmentProxy {

        android.app.Fragment mFrameworkFragment;
        android.support.v4.app.Fragment mSupportFragment;

        public FragmentProxy(android.app.Fragment fragment) {
            mFrameworkFragment = fragment;
        }

        public FragmentProxy(android.support.v4.app.Fragment fragment) {
            mSupportFragment = fragment;
        }

        public String getSimpleName() {
            return getSimpleName(isSupport() ? mSupportFragment : mFrameworkFragment);
        }

        public boolean isSupport() {
            return mFrameworkFragment == null;
        }

        public String getType() {
            return getCanonicalName(isSupport() ? mSupportFragment : mFrameworkFragment);
        }

        private static String getCanonicalName(Object o) {
            return o.getClass().getCanonicalName();
        }

        private static String getSimpleName(Object o) {
            return o.getClass().getSimpleName();
        }

    }

    private static class FragmentManagerProxy {

        Activity mActivity;
        OnBackStackChangedListener mListener;

        FragmentActivity mFragmentActivity;
        List<android.app.Fragment> mFragments;
        List<android.support.v4.app.Fragment> mSupportFragments;

        public FragmentManagerProxy(Activity activity) {
            mActivity = activity;
            if (mActivity instanceof android.support.v4.app.FragmentActivity) {
                mFragmentActivity = (FragmentActivity) mActivity;
            }
        }

        public void addOnBackStackChangedListener(OnBackStackChangedListener listener) {
            mListener = listener;
            mActivity.getFragmentManager().addOnBackStackChangedListener(mOnBackStackChangedListener);
            if (mFragmentActivity != null) {
                mFragmentActivity.getSupportFragmentManager().addOnBackStackChangedListener(mSupportOnBackStackChangedListener);
            }
        }

        public void removeOnBackStackChangedListener() {
            mListener = null;
            mActivity.getFragmentManager().removeOnBackStackChangedListener(mOnBackStackChangedListener);
            if (mFragmentActivity != null) {
                mFragmentActivity.getSupportFragmentManager().removeOnBackStackChangedListener(mSupportOnBackStackChangedListener);
            }
        }

        android.app.FragmentManager.OnBackStackChangedListener mOnBackStackChangedListener =
                new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                FragmentManager manager = mActivity.getFragmentManager();
                try {
                    Field active = manager.getClass().getDeclaredField("mActive");
                    active.setAccessible(true);
                    mFragments = (List<android.app.Fragment>) active.get(manager);
                } catch (NoSuchFieldException e) {
                    SgnLog.d(TAG, "mActive field doesn't exist in FragmentManager");
                } catch (IllegalAccessException e) {
                    SgnLog.d(TAG, "Couldn't read the value of mActive field.");
                }
                List<FragmentProxy> proxy = new ArrayList<>();
                for (android.app.Fragment fragment : mFragments) {
                    if (fragment != null) {
                        proxy.add(new FragmentProxy(fragment));
                    }
                }
                if (mListener != null) {
                    mListener.onBackStackChanged(proxy);
                }
            }
        };

        android.support.v4.app.FragmentManager.OnBackStackChangedListener mSupportOnBackStackChangedListener =
                new android.support.v4.app.FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                mSupportFragments = mFragmentActivity.getSupportFragmentManager().getFragments();
                List<FragmentProxy> fragmentProxies = new ArrayList<>();
                for (android.support.v4.app.Fragment fragment : mSupportFragments) {
                    if (fragment != null) {
                        fragmentProxies.add(new FragmentProxy(fragment));
                    }
                }
                if (mListener != null) {
                    mListener.onBackStackChanged(fragmentProxies);
                }
            }
        };

    }

    interface OnBackStackChangedListener {
        void onBackStackChanged(List<FragmentProxy> fragments);
    }

}
