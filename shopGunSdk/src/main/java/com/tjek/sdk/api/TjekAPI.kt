package com.tjek.sdk.api

import com.tjek.sdk.api.remote.APIRequest
import com.tjek.sdk.api.remote.ResponseType
import com.tjek.sdk.api.remote.models.v2.PublicationV2
import com.tjek.sdk.api.remote.models.v2.StoreV2

object TjekAPI {

    suspend fun getPublications(): ResponseType<List<PublicationV2>> {
        return APIRequest.getPublications()
    }

    suspend fun getPublication(publicationId: Id): ResponseType<PublicationV2> {
        return APIRequest.getPublication(publicationId)
    }

    suspend fun getStore(storeId: Id): ResponseType<StoreV2> {
        return APIRequest.getStore(storeId)
    }
}