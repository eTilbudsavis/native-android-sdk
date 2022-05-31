package com.tjek.sdk.api.mappers

import com.tjek.sdk.api.ValidityDateRange
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
            runDateRange = ValidityDateRange(fromDate, tillDate)
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
        return ImageUrls(
            view = v2.view ?: "",
            zoom = v2.zoom ?: "",
            thumb = v2.thumb ?: ""
        )
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
}