package com.tjek.sdk.api.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

typealias IncitoViewId = String

@Parcelize
data class IncitoOffer(
        val viewId: IncitoViewId,
        val title: String,
        val description: String?,
        val link: Uri?,
        val featureLabels: List<String>?,     // tag related to the offer to customize user experience
        val publicationId: String = ""
) : Parcelable