package com.tjek.sdk.sample.zoomlayoutsample

import android.os.Bundle
import android.preference.PreferenceActivity

class MainActivity : PreferenceActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.activity_main)
    }


}