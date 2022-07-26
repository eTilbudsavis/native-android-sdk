package com.shopgun.android.sdk.demo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainPreferenceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_layout)

        findViewById<Button>(R.id.publication_list)?.setOnClickListener {
            startActivity(Intent(this, PublicationListActivity::class.java))
        }
    }
}