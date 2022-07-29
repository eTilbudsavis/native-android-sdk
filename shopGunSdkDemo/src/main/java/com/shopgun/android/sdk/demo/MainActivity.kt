package com.shopgun.android.sdk.demo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.shopgun.android.sdk.demo.publication.PublicationListActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_layout)

        findViewById<Button>(R.id.publication_list)?.setOnClickListener {
            startActivity(Intent(this, PublicationListActivity::class.java))
        }
    }
}