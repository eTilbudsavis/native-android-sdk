@file:Suppress("MemberVisibilityCanBePrivate")

package com.tjek.sdk.api.remote

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import java.util.*

@Keep
@JsonClass(generateAdapter = true)
data class ServerResponse (
    var code: Int = 0,
    var name: String? = null,
    var message: String? = null,
    var details: String? = null)

class APIError(val serverResponse: ServerResponse) {

    enum class ErrorName(val errorName: String) {
        NotFound                        ("NOT_FOUND"),
        InvalidInput                    ("INVALID_INPUT"),
        DuplicateContent                ("DUPLICATE_CONTENT"),
        NoAccess                        ("NO_ACCESS"),
        InvalidAPIKey                   ("INVALID_API_KEY"),
        OfferNotFound                   ("OFFER_NOT_FOUND"),
        OffersNotFound                  ("OFFERS_NOT_FOUND"),
        CatalogNotFound                 ("CATALOG_NOT_FOUND"),
        IncitoNotFound                  ("INCITO_NOT_FOUND"),
        BusinessNotFound                ("DEALER_NOT_FOUND")
    }

    val knownErrorName = ErrorName.values().find { it.errorName.lowercase(Locale.ENGLISH) == serverResponse.name?.lowercase(Locale.ENGLISH) }
}