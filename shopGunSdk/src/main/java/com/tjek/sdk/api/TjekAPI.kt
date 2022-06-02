package com.tjek.sdk.api

import com.tjek.sdk.api.models.Publication

object TjekAPI {

    suspend fun getCatalogs(): List<Publication> {
        return NetworkRequest.getCatalogs()
    }
}