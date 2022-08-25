package com.tjek.sdk.api.remote.request

import com.tjek.sdk.api.Id
import com.tjek.sdk.api.IncitoData
import com.tjek.sdk.api.getV4FormattedStr
import com.tjek.sdk.api.models.*
import com.tjek.sdk.api.remote.APIClient
import com.tjek.sdk.api.remote.PaginatedResponse
import com.tjek.sdk.api.remote.ResponseType
import com.tjek.sdk.api.remote.services.BusinessService
import com.tjek.sdk.api.remote.services.OfferService
import com.tjek.sdk.api.remote.services.PublicationService
import com.tjek.sdk.api.remote.services.StoreService
import java.time.LocalDateTime

internal object APIRequest : APIRequestBase() {

    private val publicationService: PublicationService by lazy { APIClient.getClient().create(PublicationService::class.java) }
    private val storeService: StoreService by lazy { APIClient.getClient().create(StoreService::class.java) }
    private val offerService: OfferService by lazy { APIClient.getClient().create(OfferService::class.java) }
    private val businessService: BusinessService by lazy { APIClient.getClient().create(BusinessService::class.java) }

    suspend fun getPublications(
        businessIds: Array<Id>,
        storeIds: Array<Id>,
        nearLocation: LocationQuery?,
        acceptedTypes: Array<PublicationType>,
        pagination: PaginatedRequestV2
    ): ResponseType<PaginatedResponse<List<PublicationV2>>> {
        return safeApiCall(
            decoder = { list ->
                PaginatedResponse.v2PaginatedResponse(pagination,
                    list.map { PublicationV2.fromDecodable(it) })
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

    suspend fun getStores(
        offerIds: Array<Id>,
        publicationIds: Array<Id>,
        businessIds: Array<Id>,
        nearLocation: LocationQuery?,
        sortOrder: Array<StoresRequestSortOrder>,
        pagination: PaginatedRequestV2
    ): ResponseType<PaginatedResponse<List<StoreV2>>> {
        return safeApiCall(
            decoder = { list ->
                PaginatedResponse.v2PaginatedResponse(pagination,
                    list.map { StoreV2.fromDecodable(it) })
            }) {
            val params = HashMap<String, String>()
            params.putAll(pagination.v2RequestParams())
            publicationIds.takeIf { it.isNotEmpty() }?.let { params["catalog_ids"] = it.joinToString(separator = ",") }
            businessIds.takeIf { it.isNotEmpty() }?.let { params["dealer_ids"] = it.joinToString(separator = ",") }
            offerIds.takeIf { it.isNotEmpty() }?.let { params["offer_ids"] = it.joinToString(separator = ",") }
            sortOrder.takeIf { it.isNotEmpty() }?.let { array ->
                params["order_by"] = array.joinToString(separator = ",") { it.key }
            }
            nearLocation?.let { params.putAll(it.v2RequestParams()) }
            storeService.getStores(params)
        }
    }

    suspend fun getOffer(offerId: Id): ResponseType<OfferV2> {
        return safeApiCall(decoder = { offer -> OfferV2.fromDecodable(offer)}) {
            offerService.getOffer(offerId)
        }
    }

    suspend fun getOffers(
        publicationIds: Array<Id>,
        businessIds: Array<Id>,
        storeIds: Array<Id>,
        nearLocation: LocationQuery?,
        pagination: PaginatedRequestV2
    ): ResponseType<PaginatedResponse<List<OfferV2>>> {
        return safeApiCall(
            decoder = { list ->
                PaginatedResponse.v2PaginatedResponse(pagination,
                    list.map { OfferV2.fromDecodable(it) })
            }) {
            val params = HashMap<String, String>()
            params.putAll(pagination.v2RequestParams())
            publicationIds.takeIf { it.isNotEmpty() }?.let { params["catalog_ids"] = it.joinToString(separator = ",") }
            businessIds.takeIf { it.isNotEmpty() }?.let { params["dealer_ids"] = it.joinToString(separator = ",") }
            storeIds.takeIf { it.isNotEmpty() }?.let { params["store_ids"] = it.joinToString(separator = ",") }
            nearLocation?.let { params.putAll(it.v2RequestParams()) }
            offerService.getOffers(params)
        }
    }

    suspend fun getOffers(
        searchString: String,
        businessIds: Array<Id>,
        nearLocation: LocationQuery?,
        pagination: PaginatedRequestV2
    ): ResponseType<PaginatedResponse<List<OfferV2>>> {
        return safeApiCall(
            decoder = { list ->
                PaginatedResponse.v2PaginatedResponse(pagination,
                    list.map { OfferV2.fromDecodable(it) })
            }) {
            val params = HashMap<String, String>()
            params.putAll(pagination.v2RequestParams())
            params["query"] = searchString
            businessIds.takeIf { it.isNotEmpty() }?.let { params["dealer_ids"] = it.joinToString(separator = ",") }
            nearLocation?.let { params.putAll(it.v2RequestParams()) }
            offerService.getOffersSearch(params)
        }
    }

    suspend fun getOfferFromIncito(
        viewId: IncitoViewId,
        publicationId: Id
    ): ResponseType<OfferV4> {
        return safeApiCall(decoder = { offerContainer -> OfferV4.fromDecodable(offerContainer.offer) }) {
            offerService.getOfferFromIncito(IncitoOfferAPIQuery(
                viewId = viewId,
                publicationId = publicationId
            ))
        }
    }

    suspend fun getBusiness(businessId: Id): ResponseType<BusinessV2> {
        return safeApiCall(decoder = { business -> BusinessV2.fromDecodable(business)}) {
            businessService.getDealer(businessId)
        }
    }

    suspend fun getPublicationPages(
        publicationId: Id,
        aspectRatio: Double? = null
    ): ResponseType<List<PublicationPageV2>> {
        return safeApiCall(
            decoder = { list ->
                list.mapIndexed { pageIndex, images ->
                    PublicationPageV2(pageIndex, "${pageIndex + 1}", aspectRatio ?: 1.0, images)
                }
            }) {
            publicationService.getCatalogPages(publicationId)
        }
    }

    suspend fun getPublicationHotspots(
        publicationId: Id,
        width: Double,
        height: Double
    ): ResponseType<List<PublicationHotspotV2>> {
        return safeApiCall(
            decoder = { list ->
                list.map { PublicationHotspotV2.fromDecodable(it) }
                    .onEach { it.normalize(width, height) }
            }) {
            publicationService.getCatalogHotspots(publicationId)
        }
    }

    suspend fun getIncito(
        id: Id,
        deviceCategory: IncitoDeviceCategory,
        orientation: IncitoOrientation,
        pixelRatio: Float,
        maxWidth: Int,
        featureLabels: List<FeatureLabel>?,
        locale: String?
    ): ResponseType<IncitoData> {
        return safeApiCall(
            decoder = { it }) {
            publicationService.getIncito(IncitoAPIQuery(
                id = id,
                deviceCategory = deviceCategory,
                orientation = orientation,
                pixelRatio = pixelRatio,
                maxWidth = maxWidth,
                locale = locale,
                time = LocalDateTime.now().getV4FormattedStr(),
                featureLabels = featureLabels
            ))
        }
    }
}