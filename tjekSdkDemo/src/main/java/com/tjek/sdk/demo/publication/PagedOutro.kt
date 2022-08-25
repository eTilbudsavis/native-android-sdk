package com.tjek.sdk.demo.publication

import android.content.Context
import android.widget.TextView
import com.tjek.sdk.demo.R
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.publicationviewer.paged.views.OutroView
import com.tjek.sdk.publicationviewer.paged.OutroViewGenerator

// Example of how to configure view for Outro.

class OutroConfig: OutroViewGenerator() {
    override fun getOutroView(context: Context, page: Int): OutroView {
        return DemoOutroView(context, page, publication)
    }
}

class DemoOutroView(
    context: Context,
    page: Int,
    publication: PublicationV2?
) : OutroView(context, page, publication)
{

    init {
        inflate(context, R.layout.intro_outro_layout, this)
        findViewById<TextView>(R.id.intro)?.text = "END of ${publication?.branding?.name} publication"
    }
}
