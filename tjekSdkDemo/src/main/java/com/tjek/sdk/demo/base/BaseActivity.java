package com.tjek.sdk.demo.base;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;

    protected void showProgress(String title, String message) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, title, message, true, true);
        }
    }

    protected void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

}
