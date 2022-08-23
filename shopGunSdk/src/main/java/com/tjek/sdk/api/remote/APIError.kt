package com.tjek.sdk.api.remote

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import java.util.*

@Keep
@JsonClass(generateAdapter = true)
data class APIError (
    var code: Int = 0,
    var name: String? = null,
    var message: String? = null,
    var details: String? = null
) {
    override fun toString(): String {
        return "name=$name, message=$message, details=$details"
    }
}
