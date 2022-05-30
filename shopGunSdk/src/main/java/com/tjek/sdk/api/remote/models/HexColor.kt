package com.tjek.sdk.api.remote.models

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson
import com.tjek.sdk.TjekLogCat
import java.lang.NumberFormatException

@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class HexColor

/** Converts strings like #ff0000 to the corresponding color ints. */
class ColorAdapter {
    @ToJson
    fun toJson(@HexColor rgb: Int?): String {
        return String.format("#%06x", rgb ?: 0)
    }

    @FromJson
    @HexColor
    fun fromJson(rgb: String?): Int? {
        // it has to return a nullable type (nullable in the correspondent data class)
        return if (rgb == null || rgb.length < 2 ) 0
        else try {
            Integer.parseInt(rgb.substring(1), 16)
        } catch (e: NumberFormatException) {
            TjekLogCat.e("fail to parse HexColor $rgb")
            null
        }
    }
}