package com.shopgun.android.sdk.demo

import android.content.Context
import android.widget.TextView
import com.tjek.sdk.publicationviewer.paged.IntroOutroView
import com.tjek.sdk.publicationviewer.paged.OutroConfiguration

class OutroConfig: OutroConfiguration(hasOutro = true) {
    override fun getOutroView(context: Context, page: Int): IntroOutroView {
        return OutroView(context, page)
    }
}


class OutroView(context: Context, page: Int) : IntroOutroView(context, page) {

    init {
        inflate(context, R.layout.intro_layout, this)
        findViewById<TextView>(R.id.intro)?.text = "Goodbye!!!"
    }
}