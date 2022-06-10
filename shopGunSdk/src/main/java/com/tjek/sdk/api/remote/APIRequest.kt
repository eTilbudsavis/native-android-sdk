package com.tjek.sdk.api.remote

import com.tjek.sdk.api.Id
import com.tjek.sdk.api.remote.models.v2.PublicationV2
import com.tjek.sdk.api.remote.models.v2.StoreV2
import com.tjek.sdk.api.remote.services.PublicationService
import com.tjek.sdk.api.remote.services.StoreService

internal object APIRequest : APIRequestBase() {

    private val publicationService: PublicationService by lazy { APIClient.getClient().create(PublicationService::class.java) }
    private val storeService: StoreService by lazy { APIClient.getClient().create(StoreService::class.java) }

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

    suspend fun getStore(storeId: Id): ResponseType<StoreV2> {
        return safeApiCall(decoder = { store -> StoreV2.fromDecodable(store)}) {
            storeService.getStore(storeId)
        }
    }
}