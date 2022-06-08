package com.tjek.sdk.api

import com.tjek.sdk.api.remote.APIRequest
import com.tjek.sdk.api.remote.ResponseType
import com.tjek.sdk.api.remote.models.v2.PublicationV2

object TjekAPI {

    suspend fun getPublications(): ResponseType<List<PublicationV2>> {
        return APIRequest.getPublications()
    }

    suspend fun getPublication(publicationId: Id): ResponseType<PublicationV2> {
        return APIRequest.getPublication(publicationId)
    }
}