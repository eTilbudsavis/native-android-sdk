package com.tjek.sdk.api

import com.tjek.sdk.api.models.Publication

object TjekAPI {

    suspend fun getPublications(): List<Publication> {
        return NetworkRequest.getPublications()
    }

    suspend fun getPublication(publicationId: Id): Publication {
        return NetworkRequest.getPublication(publicationId)
    }
}