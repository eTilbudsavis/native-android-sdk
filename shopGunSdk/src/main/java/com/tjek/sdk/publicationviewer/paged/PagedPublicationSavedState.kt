package com.tjek.sdk.publicationviewer.paged

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PagedPublicationSavedState(
    val config: PagedPublicationConfiguration,
    val hasSentOpenEvent: Boolean
): Parcelable
