package com.tjek.sdk.api.remote.request

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.tjek.sdk.api.Id

private val knownVersions = listOf("1.0.0")

@Suppress("EnumEntryName")
enum class IncitoDeviceCategory { mobile, tablet, desktop }

@Suppress("EnumEntryName")
enum class IncitoOrientation { vertical, horizontal }

@Suppress("EnumEntryName")
enum class IncitoPointerType { fine, coarse }

@Keep
@JsonClass(generateAdapter = true)
data class IncitoAPIQuery(
    val id: Id = "",
    @Json(name = "device_category")
    val deviceCategory: IncitoDeviceCategory = IncitoDeviceCategory.mobile,
    val orientation: IncitoOrientation = IncitoOrientation.vertical,
    @Json(name = "pixel_ratio")
    val pixelRatio: Float = 1F,
    @Json(name = "max_width")
    val maxWidth: Int = 100,
    @Json(name = "locale_code")
    val locale: String?,
    val time: String?,
    @Json(name = "feature_labels")
    val featureLabels: List<FeatureLabel>?,
    @Json(name = "versions_supported")
    val versionsSupported: List<String> = knownVersions,
    val pointer: IncitoPointerType = IncitoPointerType.coarse)

@Keep
@JsonClass(generateAdapter = true)
data class FeatureLabel(
    val key: String = "",
    val value: Float = 0F)

@Keep
@JsonClass(generateAdapter = true)
data class IncitoOfferAPIQuery(
    @Json(name = "view_id")
    val viewId: String = "",
    @Json(name = "publication_id")
    val publicationId: String = "")