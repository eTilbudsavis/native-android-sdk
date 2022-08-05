package com.shopgun.android.sdk.demo.publication

import android.content.Context
import android.widget.TextView
import com.shopgun.android.sdk.demo.R
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.publicationviewer.paged.IntroConfiguration
import com.tjek.sdk.publicationviewer.paged.views.IntroOutroView
import com.tjek.sdk.publicationviewer.paged.OutroConfiguration

// Examples of how to configure view for Intro and Outro.

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
        inflate(context, R.layout.intro_outro_layout, this)
        findViewById<TextView>(R.id.intro)?.text = "END of ${publication?.branding?.name} publication"
    }
}

//--------------------------------------------------------------------------------------------------

class IntroConfig: IntroConfiguration() {
    override fun getIntroView(context: Context, page: Int): IntroOutroView {
        return IntroView(context, page, publication)
    }
}

class IntroView(
    context: Context,
    page: Int,
    publication: PublicationV2?
) : IntroOutroView(context, page, publication
) {

    init {
        inflate(context, R.layout.intro_outro_layout, this)
        findViewById<TextView>(R.id.intro)?.text = "START of ${publication?.branding?.name} publication"
    }
}