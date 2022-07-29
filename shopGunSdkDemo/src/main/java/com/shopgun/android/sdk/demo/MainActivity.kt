package com.shopgun.android.sdk.demo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.shopgun.android.sdk.demo.Constants.TJEK_HQ
import com.shopgun.android.sdk.demo.publication.PublicationListActivity
import com.tjek.sdk.api.models.Coordinate

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_layout)

        val lat = findViewById<EditText>(R.id.lat)
        lat?.setText(TJEK_HQ.latitude.toString())
        val lng = findViewById<EditText>(R.id.lng)
        lng?.setText(TJEK_HQ.longitude.toString())

        findViewById<Button>(R.id.publication_list)?.setOnClickListener {
            val coordinate = Coordinate(lat.text.toString().toDouble(), lng.text.toString().toDouble())
            startActivity(Intent(this, PublicationListActivity::class.java).apply {
                putExtra("coordinate", coordinate)
            })
        }
    }
}