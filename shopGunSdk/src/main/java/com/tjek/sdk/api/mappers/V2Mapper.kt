package com.tjek.sdk.api.mappers

import com.tjek.sdk.api.distantFuture
import com.tjek.sdk.api.distantPast
import com.tjek.sdk.api.models.*
import com.tjek.sdk.api.parse
import com.tjek.sdk.api.remote.models.v2.*

object V2Mapper {

    fun map(v2: PublicationV2): Publication {
        // sanity check on the dates
        val fromDate = v2.runFromDateStr?.parse() ?: distantPast()
        val tillDate = v2.runTillDateStr?.parse() ?: distantFuture()

        return Publication(
            id = v2.id ?: "",
            width = v2.dimensions?.width ?: 1f,
            height = v2.dimensions?.height ?: 1f,
            offerCount = v2.offerCount ?: 0,
            pageCount = v2.pageCount ?: 0,
            label = v2.label ?: "",
            isAvailableInAllStores = v2.allStores ?: true,
            businessId = v2.dealerId ?: "",
            storeId = v2.storeId  ?: "",
            branding = v2.branding?.let { map(it) } ?: Branding(),
            frontPageImageUrls = v2.frontPageImageUrls?.let { map(it) } ?: ImageUrls(),
            types = v2.types?.let { map(it) } ?: listOf(PublicationTypes.Paged),
            runDateRange = minOf(fromDate, tillDate)..maxOf(fromDate, tillDate)
        )
    }

    fun map(v2: BrandingV2): Branding {
        return Branding(
            name = v2.name ?: "",
            websiteUrl = v2.website ?: "",
            description = v2.description ?: "",
            logoURL = v2.logoURL ?: "",
            hexColor = v2.color ?: 0
        )
    }

    fun map(v2: ImageUrlsV2): ImageUrls {
        val view = ImageData(
            width = avgViewWidth,
            url = v2.view ?: ""
        )
        val zoom = ImageData(
            width = avgZoomWidth,
            url = v2.zoom ?: ""
        )
        val thumb = ImageData(
            width = avgThumbWidth,
            url = v2.thumb ?: ""
        )

        return ImageUrls(listOf(view, zoom, thumb))
    }

    fun map(v2: List<PublicationTypesV2>): List<PublicationTypes> {
        return when {
            v2.size == 1 && v2.contains(PublicationTypesV2.paged) -> listOf(PublicationTypes.Paged)
            v2.size == 1 && v2.contains(PublicationTypesV2.incito) -> listOf(PublicationTypes.Incito)
            v2.containsAll(listOf(PublicationTypesV2.paged, PublicationTypesV2.incito)) -> listOf(PublicationTypes.Paged, PublicationTypes.Incito)
            else -> listOf(PublicationTypes.Paged)
        }
    }

    fun map(v2: DealerV2): Business {
        return Business(
            id = v2.id ?: "",
            name = v2.name ?: "",
            websiteUrl = v2.website ?: "",
            description = v2.description ?: "",
            descriptionMarkdown = v2.descriptionMarkdown ?: "",
            logoOnWhiteUrl = v2.logoOnWhiteUrl ?: "",
            logoOnBrandColorUrl = v2.pageFlip?.logoURL ?: "",
            brandHexColor = v2.color ?: 0,
            country = v2.country?.id ?: ""
        )
    }

    fun map(v2: OfferV2): Offer {
        // sanity check on the dates
        val fromDate = v2.runFromDateStr?.parse() ?: distantPast()
        val tillDate = v2.runTillDateStr?.parse() ?: distantFuture()

        val prePrice = (v2.price?.prePrice?.takeIf { it > 0 } ?: 0) as Double
        val price = (v2.price?.price?.takeIf { it > 0 } ?: 0) as Double

        val pieceCountFrom = (v2.quantity?.pieces?.from?.takeIf { it > 0 } ?: 1).toFloat()
        val pieceCountTo = (v2.quantity?.pieces?.to?.takeIf { it > 0 } ?: 1).toFloat()

        val sizeFrom = (v2.quantity?.size?.from?.takeIf { it > 0 } ?: 0).toFloat()
        val sizeTo = (v2.quantity?.size?.to?.takeIf { it > 0 } ?: 0).toFloat()

        return Offer(
            id = v2.id ?: "",
            heading = v2.heading ?: "",
            description = v2.description ?: "",
            webshopUrl = v2.links?.webshop ?: "",
            runDateRange = minOf(fromDate, tillDate)..maxOf(fromDate, tillDate),
            visibleFrom = v2.publishDateStr?.parse() ?: distantPast(),
            price = v2.price?.price?.toFloat() ?: 0f,
            currency = v2.price?.currency ?: "",
            savings = (prePrice - price).toFloat().takeIf { it > 0 } ?: 0f,
            pieceCount = minOf(pieceCountFrom, pieceCountTo)..maxOf(pieceCountFrom, pieceCountTo),
            unitSize = minOf(sizeFrom, sizeTo)..maxOf(sizeFrom, sizeTo),
            unitSymbol = QuantityUnit.fromSymbol(v2.quantity?.unit?.symbol ?: QuantityUnit.Piece.symbol),
            branding = v2.branding?.let { map(it) } ?: Branding(),
            businessId = v2.dealerId ?: "",
            storeId = v2.storeId ?: "",
            publicationInfo = PublicationInfo(
                publicationId = v2.catalogId ?: "",
                pagedPublicationPage = v2.catalogPage?.let { if (v2.catalogPage > 0) v2.catalogPage - 1 else 0 } ?: 0,
                incitoViewId = v2.catalogViewId ?: ""
            ),
            imageUrls = v2.imageUrls?.let { map(it) } ?: ImageUrls()
        )
    }
}