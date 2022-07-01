package com.tjek.sdk.publicationviewer.paged

import android.content.Context
import android.widget.FrameLayout
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.publicationviewer.paged.libs.verso.VersoPageView
import com.tjek.sdk.publicationviewer.paged.libs.verso.VersoPageViewListener

abstract class IntroOutroView(
    context: Context,
    override val page: Int,
    val publication: PublicationV2?
) : FrameLayout(context), VersoPageView {

    override fun onZoom(scale: Float): Boolean = false

    override fun setOnLoadCompleteListener(listener: VersoPageViewListener.OnLoadCompleteListener) { }

    override fun onVisible() { }

    override fun onInvisible() { }

}