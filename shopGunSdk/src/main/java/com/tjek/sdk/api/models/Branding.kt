package com.tjek.sdk.api.models

import android.os.Parcelable
import com.tjek.sdk.api.HexColor
import kotlinx.parcelize.Parcelize

@Parcelize
data class Branding(
    val name: String = "",
    val websiteUrl: String = "",
    val description: String = "",
    val logoURL: String = "",
    val hexColor: HexColor = 0
) : Parcelable