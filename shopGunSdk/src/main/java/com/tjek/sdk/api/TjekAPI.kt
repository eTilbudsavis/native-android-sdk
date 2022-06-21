package com.tjek.sdk.api

import com.tjek.sdk.api.models.OfferV2
import com.tjek.sdk.api.models.PublicationType
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.api.models.StoreV2
import com.tjek.sdk.api.remote.*
import com.tjek.sdk.api.remote.APIRequest

object TjekAPI {

    /**
    A request that returns a paginated list of publications, limited by the parameters.

    - Parameters:
        - businessIds: Limit the list of publications by the id of the business that published them.
        - storeIds: Limit the list of publications by the ids of the stores they cover.
        - near: Specify a coordinate to return publications in relation to. Also optionally limit the publications to within a max radius from that coordinate.
        - acceptedTypes: Choose which types of publications to return (defaults to all)
    - pagination: The count & cursor of the request's page. Defaults to the first page of 24 publications. `itemCount` must not be more than 100. `startCursor` must not be greater than 1000.
    - Returns:
        A list of `PublicationV2`.
     */
    suspend fun getPublications(
        businessIds: Array<Id> = emptyArray(),
        storeIds: Array<Id> = emptyArray(),
        nearLocation: LocationQuery? = null,
        acceptedTypes: Array<PublicationType> = PublicationType.values(),
        pagination: PaginatedRequest<Int> = PaginatedRequest.v2FirstPage(24)
    ): ResponseType<PaginatedResponse<List<PublicationV2>>> {
        return APIRequest.getPublications(businessIds, storeIds, nearLocation, acceptedTypes, pagination)
    }


    /**
    A request that asks for a specific publication, based on its Id.

    - Parameters:
        - publicationId: The Id of the specific publication we are looking for.
    - Returns: a response type of `PublicationV2`.
     */
    suspend fun getPublication(publicationId: Id): ResponseType<PublicationV2> {
        return APIRequest.getPublication(publicationId)
    }

    /**
    A request that asks for a specific store, based on its Id.

    - Parameters:
        - storeId: The Id of the specific store we are looking for.
    - Returns:
        A response type of `StoreV2`.
     */
    suspend fun getStore(storeId: Id): ResponseType<StoreV2> {
        return APIRequest.getStore(storeId)
    }

    /**
    A request that asks for a specific offer, based on its Id.

    - Parameters:
        - offerId: The Id of the specific offer we are looking for.
    - Returns:
        A response type of `OfferV2`.
     */
    suspend fun getOffer(offerId: Id): ResponseType<OfferV2> {
        return APIRequest.getOffer(offerId)
    }

    /**
    A request that returns a paginated list of offers, limited by the parameters.

    - Parameters:
        - publicationIds: Limit the list of offers by the id of the publication that its in.
        - businessIds: Limit the list of offers by the id of the business that published them.
        - storeIds: Limit the list of offers by the ids of the stores they are in.
        - near: Specify a coordinate to return offers in relation to. Also optionally limit the offers to within a max radius from that coordinate.
        - pagination: The count & cursor of the request's page. Defaults to the first page of 24 offers. `itemCount` must not be more than 100. `startCursor` must not be greater than 1000.
    - Returns:
        A list of `OfferV2` in a paginated object.
     */
    suspend fun getOffers(
        publicationIds: Array<Id> = emptyArray(),
        businessIds: Array<Id> = emptyArray(),
        storeIds: Array<Id> = emptyArray(),
        nearLocation: LocationQuery? = null,
        pagination: PaginatedRequest<Int> = PaginatedRequest.v2FirstPage(24)
    ): ResponseType<PaginatedResponse<List<OfferV2>>> {
        return APIRequest.getOffers(publicationIds, businessIds, storeIds, nearLocation, pagination)
    }
}