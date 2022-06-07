package com.tjek.sdk.api.models

import android.os.Parcelable
import com.tjek.sdk.api.HexColor
import com.tjek.sdk.api.Id
import kotlinx.parcelize.Parcelize

@Parcelize
data class Business(
    val id: Id = "",
    val name: String = "",
    val websiteUrl: String = "",
    val description: String = "",
    val descriptionMarkdown: String = "",
    val logoOnWhiteUrl: String = "",
    val logoOnBrandColorUrl: String = "",
    val brandHexColor: HexColor = 0,
    val country: String = ""
) : Parcelable
