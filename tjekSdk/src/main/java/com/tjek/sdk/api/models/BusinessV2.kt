package com.tjek.sdk.api.models
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
import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.tjek.sdk.api.HexColorStr
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


//------------- Classes used for decoding api responses -------------//

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
    val country: CountryV2Decodable
)

@Keep
@JsonClass(generateAdapter = true)
data class PageFlipV2(
    val logo: String,
    val color: String?,
)