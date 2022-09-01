package com.tjek.sdk.publicationviewer.incito
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
import com.tjek.sdk.api.models.IncitoOffer
import com.tjek.sdk.api.models.IncitoViewId
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

    /**
     * If you're interested in having a list of all the available offers in the incito,
     * this callback will give you the list of all the offers available.
     * To jump to a specific offer, use the key, `IncitoViewId`, to jump to it calling `IncitoPublicationFragment.goToOffer`.
     */
    fun onOfferListReady(offers: Map<IncitoViewId, IncitoOffer>)
}