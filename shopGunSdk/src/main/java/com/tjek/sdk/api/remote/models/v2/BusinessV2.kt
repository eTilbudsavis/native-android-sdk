package com.tjek.sdk.api.remote.models.v2

import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.tjek.sdk.api.Id
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
    val brandColorHex: String?,
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

@Keep
@JsonClass(generateAdapter = true)
data class BusinessV2Decodable(
    val id:Id,
    val name: String,
    val website: String?,
    val description: String?,
    @Json(name = "description_markdown")
    val descriptionMarkdown: String?,
    @Json(name = "logo")
    val logoOnWhite: String,
    val color: String?,
    @Json(name = "pageflip")
    val pageFlip: PageFlipV2,
    val country: CountryV2
)

@Keep
@JsonClass(generateAdapter = true)
data class PageFlipV2(
    val logo: String,
    val color: String?,
)