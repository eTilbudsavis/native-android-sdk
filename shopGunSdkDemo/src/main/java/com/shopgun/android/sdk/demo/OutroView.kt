package com.shopgun.android.sdk.demo

import android.content.Context
import android.widget.TextView
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.publicationviewer.paged.IntroOutroView
import com.tjek.sdk.publicationviewer.paged.OutroConfiguration

class OutroConfig: OutroConfiguration() {
    override fun getOutroView(context: Context, page: Int): IntroOutroView {
        return OutroView(context, page, publication)
    }
}


class OutroView(
    context: Context,
    page: Int,
    publication: PublicationV2?
) : IntroOutroView(context, page, publication
) {

    init {
        inflate(context, R.layout.intro_layout, this)
        findViewById<TextView>(R.id.intro)?.text = "End of ${publication?.branding?.name} publication"
    }
}