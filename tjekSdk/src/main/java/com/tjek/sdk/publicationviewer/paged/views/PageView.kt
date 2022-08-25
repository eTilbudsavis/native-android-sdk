package com.tjek.sdk.publicationviewer.paged.views
/*
 * Copyright (C) 2022 Tjek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BaseTarget
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.transition.Transition
import com.tjek.sdk.ColorInt
import com.tjek.sdk.api.models.PublicationPageV2
import com.tjek.sdk.publicationviewer.paged.libs.verso.VersoPageView
import com.tjek.sdk.publicationviewer.paged.libs.verso.VersoPageViewListener
import com.tjek.sdk.publicationviewer.paged.layouts.AspectRatioFrameLayout
import com.tjek.sdk.publicationviewer.paged.utils.UnitUtils

private enum class Size{
    Thumb, View, Zoom
}

@SuppressLint("SetTextI18n")
class PageView(
    context: Context,
    private val publicationPage: PublicationPageV2?,
    showPageNumberWhileLoading: Boolean = true,
    textColor: ColorInt = Color.TRANSPARENT
) : AspectRatioFrameLayout(context), VersoPageView {

    private var size: Size? = null
    private lateinit var imageView: ImageView
    private var pulsatingTextView: PulsatingTextView? = null
    private var loadCompletionListener: VersoPageViewListener.OnLoadCompleteListener? = null

    private val pageTarget = object : BaseTarget<Drawable>() {

        private var callback = true

        override fun onResourceReady(
            resource: Drawable,
            transition: Transition<in Drawable>?
        ) {
            pulsatingTextView?.visibility = View.GONE
            imageView.setImageDrawable(resource)
            loadCompletionListener?.let {
                if (callback) {
                    callback = false
                    it.onPageLoadComplete(true, this@PageView)
                }
            }
        }

        override fun getSize(cb: SizeReadyCallback) {
            cb.onSizeReady(SIZE_ORIGINAL, SIZE_ORIGINAL)
        }

        override fun removeCallback(cb: SizeReadyCallback) { }

    }

    init {
        aspectRatio = publicationPage?.aspectRatio?.toFloat() ?: 1f

        //Add the ImageView
        imageView = PageImageView(context)
        addView(imageView)

        // Add the pulsating number
        val lp = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.gravity = Gravity.CENTER
        if (showPageNumberWhileLoading) {
            pulsatingTextView = PulsatingTextView(context).apply {
                layoutParams = lp
                setPulseColors(textColor, 20, 80)
                text = publicationPage?.let { "${publicationPage.index + 1}" } ?: ""
                textSize = UnitUtils.spToPx(26, context).toFloat()
            }
            addView(pulsatingTextView)
        }
    }

    override fun setOnLoadCompleteListener(listener: VersoPageViewListener.OnLoadCompleteListener) {
        loadCompletionListener = listener
    }

    override fun onZoom(scale: Float): Boolean {
        when {
            scale > 1.1f && !isZoomed() -> load(Size.Zoom)
            scale < 1.1f && isZoomed() -> load(Size.View)
        }
        return false
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        load(Size.View)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Glide.with(context).clear(pageTarget)
    }

    override fun onVisible() { }

    override fun onInvisible() { }

    override val page: Int
        get() = publicationPage?.index ?: 0

    private fun isZoomed() = size == Size.Zoom

    private fun load(size: Size) {
        if (this.size == size) return
        this.size = size
        Glide.with(context).clear(pageTarget)
        Glide.with(context)
            .load(when(size) {
                Size.Thumb -> publicationPage?.images?.thumb
                Size.View -> publicationPage?.images?.view
                Size.Zoom -> publicationPage?.images?.zoom
            })
            .into(pageTarget)
    }
}