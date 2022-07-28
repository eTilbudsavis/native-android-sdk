package com.tjek.sdk.publicationviewer.incito

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class IncitoPublicationSavedState(
    val config: IncitoPublicationConfiguration,
    val hasSentOpenEvent: Boolean
): Parcelable
