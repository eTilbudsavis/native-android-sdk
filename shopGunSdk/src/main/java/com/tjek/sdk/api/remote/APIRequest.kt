package com.tjek.sdk.api.remote

import com.tjek.sdk.api.Id
import com.tjek.sdk.api.models.PublicationType
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.api.models.StoreV2
import com.tjek.sdk.api.remote.services.PublicationService
import com.tjek.sdk.api.remote.services.StoreService

internal object APIRequest : APIRequestBase() {

    private val publicationService: PublicationService by lazy { APIClient.getClient().create(PublicationService::class.java) }
    private val storeService: StoreService by lazy { APIClient.getClient().create(StoreService::class.java) }

    suspend fun getPublications(
        businessIds: Array<Id>,
        storeIds: Array<Id>,
        nearLocation: LocationQuery?,
        acceptedTypes: Array<PublicationType>,
        pagination: PaginatedRequest<Int>
    ): ResponseType<PaginatedResponse<List<PublicationV2>>> {
        return safeApiCall(
            decoder = { list ->
                PaginatedResponse.v2PaginatedResponse(pagination, list.map { PublicationV2.fromDecodable(it)})
            }) {
            val params = HashMap<String, String>()
            params["types"] = acceptedTypes.joinToString(separator = ",")
            businessIds.takeIf { it.isNotEmpty() }?.let { params["dealer_ids"] = it.joinToString(separator = ",") }
            storeIds.takeIf { it.isNotEmpty() }?.let { params["store_ids"] = it.joinToString(separator = ",") }
            nearLocation?.let { params.putAll(it.v2RequestParams()) }
            params.putAll(pagination.v2RequestParams())
            publicationService.getCatalogs(params)
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