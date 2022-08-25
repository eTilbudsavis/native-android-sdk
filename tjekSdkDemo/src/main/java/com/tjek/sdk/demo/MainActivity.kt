package com.tjek.sdk.demo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.tjek.sdk.demo.Constants.OSLO
import com.tjek.sdk.demo.Constants.STOCKHOLM
import com.tjek.sdk.demo.Constants.TJEK_HQ
import com.tjek.sdk.demo.R
import com.tjek.sdk.demo.publication.PublicationListActivity
import com.tjek.sdk.api.models.Coordinate

class MainActivity : AppCompatActivity() {

    private var lat: EditText? = null
    private var lng: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_layout)

        lat = findViewById(R.id.lat)
        lng = findViewById(R.id.lng)
        setCoordinates(TJEK_HQ)

        findViewById<Button>(R.id.publication_list)?.setOnClickListener {
            val coordinate = Coordinate(lat?.text.toString().toDouble(), lng?.text.toString().toDouble())
            startActivity(Intent(this, PublicationListActivity::class.java).apply {
                putExtra("coordinate", coordinate)
            })
        }

        findViewById<Button>(R.id.tjek_hq)?.setOnClickListener { setCoordinates(TJEK_HQ) }
        findViewById<Button>(R.id.oslo)?.setOnClickListener { setCoordinates(OSLO) }
        findViewById<Button>(R.id.stockholm)?.setOnClickListener { setCoordinates(STOCKHOLM) }
    }

    private fun setCoordinates(coordinate: Coordinate){
        lat?.setText(coordinate.latitude.toString())
        lng?.setText(coordinate.longitude.toString())

    }
}