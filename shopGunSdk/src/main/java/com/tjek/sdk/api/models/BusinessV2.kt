package com.tjek.sdk.api.models

import android.os.Parcelable
import com.tjek.sdk.api.HexColorStr
import com.tjek.sdk.api.Id
import com.tjek.sdk.api.remote.models.v2.BusinessV2Decodable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BusinessV2(
    val id: Id,
    val name: String,
    val website: String?,
    val description: String?,
    val descriptionMarkdown: String?,
    val logoOnWhite: String,
    val logoOnBrandColor: String,
    val brandColorHex: HexColorStr?,
    val country: String
) : Parcelable {

    companion object {
        fun fromDecodable(b: BusinessV2Decodable): BusinessV2 {
            return BusinessV2(
                id = b.id,
                name = b.name,
                website = b.website,
                description = b.description,
                descriptionMarkdown = b.descriptionMarkdown,
                logoOnWhite = b.logoOnWhite,
                brandColorHex = b.color,
                logoOnBrandColor = b.pageFlip.logo,
                country = b.country.id
            )
        }
    }
}