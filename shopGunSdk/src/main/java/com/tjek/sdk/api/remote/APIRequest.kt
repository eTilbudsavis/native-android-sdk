package com.tjek.sdk.api.remote

import com.tjek.sdk.api.Id
import com.tjek.sdk.api.remote.models.v2.PublicationV2
import com.tjek.sdk.api.remote.services.PublicationService

internal object APIRequest : APIRequestBase() {

    private val publicationService: PublicationService by lazy { APIClient.getClient().create(PublicationService::class.java) }

    suspend fun getPublications(): ResponseType<List<PublicationV2>> {
        return safeApiCall(decoder = { list -> list.map { PublicationV2.fromDecodable(it)}}) {
            publicationService.getCatalogs()
        }
    }

    suspend fun getPublication(publicationId: Id): ResponseType<PublicationV2> {
        return safeApiCall(decoder = { publication -> PublicationV2.fromDecodable(publication)}) {
            publicationService.getCatalog(publicationId)
        }
    }
}