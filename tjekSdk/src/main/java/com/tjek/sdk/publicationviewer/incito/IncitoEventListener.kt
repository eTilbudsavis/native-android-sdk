package com.tjek.sdk.publicationviewer.incito

import com.tjek.sdk.api.models.IncitoOffer
import com.tjek.sdk.api.models.PublicationV2

interface IncitoEventListener {

    /**
     * - progress: percentage for the scrolling of the incito.
     * If you want to use a progress bar, you can set its progress with
     * (progress * 100).roundToInt().
     *
     * - verticalScrollOffset: scrolling offset for the webview.
     * Save this value if you want to reopen the incito from the last position.
     * It'll be used in webview.scrollTo(0, verticalScrollOffset)
     */
    fun onProgressChanged(progress: Float, verticalScrollOffset: Int)

    fun onOfferClick(incitoOffer: IncitoOffer, publicationV2: PublicationV2?)
    fun onOfferLongClick(incitoOffer: IncitoOffer, publicationV2: PublicationV2?)

}