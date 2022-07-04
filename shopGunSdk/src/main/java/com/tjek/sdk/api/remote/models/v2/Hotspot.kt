package com.tjek.sdk.api.remote.models.v2

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.tjek.sdk.api.Id
import com.tjek.sdk.api.ValidityDateStr
import com.tjek.sdk.api.remote.RawJson
import okio.ByteString
import org.json.JSONObject

@Keep
@JsonClass(generateAdapter = true)
data class HotspotOfferV2Decodable (
    val id: Id,
    val heading: String,
    @Json(name = "run_from")
    val runFromDateStr: ValidityDateStr?,
    @Json(name = "run_till")
    val runTillDateStr: ValidityDateStr?,
    @Json(name = "publish")
    val publishDateStr: ValidityDateStr?,
    @Json(name = "pricing")
    val price: PriceV2?,
    val quantity: QuantityV2?
)

@Keep
@JsonClass(generateAdapter = true)
data class PublicationHotspotV2Decodable (
    val offer: HotspotOfferV2Decodable?,
    @RawJson val locations: ByteString?
)