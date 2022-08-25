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
import java.util.*

@Parcelize
data class BusinessV4(
    val id: Id,
    val name: String,
    val countryCode: String,
    val primaryColorHex: HexColorStr,
    val logotypesForWhite: List<ImageV4>,
    val logotypesForPrimary: List<ImageV4>,
    val shortDescription: String?,
    val website: String?
): Parcelable {

    companion object {
        fun fromDecodable(b: BusinessV4Decodable): BusinessV4 {
            return BusinessV4(
                id = b.id,
                name = b.name,
                countryCode = b.countryCode.uppercase(Locale.ENGLISH),
                primaryColorHex = b.primaryColor,
                logotypesForWhite = b.logotypesForWhite,
                logotypesForPrimary = b.logotypesForPrimary,
                shortDescription = b.description,
                website = b.website
            )
        }
    }
}


//------------- Classes used for decoding api responses -------------//

@Keep
@JsonClass(generateAdapter = true)
data class BusinessV4Decodable(
    val id: Id,
    val name: String,
    @Json(name = "primary_color")
    val primaryColor: String,
    @Json(name = "positive_logotypes")
    val logotypesForWhite: List<ImageV4>,
    @Json(name = "negative_logotypes")
    val logotypesForPrimary: List<ImageV4>,
    @Json(name = "country_code")
    val countryCode: String,
    @Json(name = "short_description")
    val description: String?,
    @Json(name = "website_link")
    val website: String?
)
