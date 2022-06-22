package com.tjek.sdk.api.models

import android.os.Parcelable
import android.util.SparseArray
import com.tjek.sdk.api.*
import com.tjek.sdk.api.remote.models.v2.HotspotOfferV2Decodable
import com.tjek.sdk.api.remote.models.v2.PriceV2
import com.tjek.sdk.api.remote.models.v2.PublicationHotspotV2Decodable
import com.tjek.sdk.api.remote.models.v2.QuantityV2
import com.tjek.sdk.publicationviewer.paged.PolygonF
import kotlinx.parcelize.Parcelize

@Parcelize
data class PublicationHotspotV2(
    val offer: HotspotOfferV2?,
    val pageLocations: SparseArray<PolygonF> = SparseArray()
): Parcelable {

    companion object {
        fun fromDecodable(h: PublicationHotspotV2Decodable): PublicationHotspotV2 {
            val pageLocations = SparseArray<PolygonF>()
            h.locations?.keys()?.let {
                while (it.hasNext()) {
                    val page = it.next()
                    val intPage = Integer.valueOf(page) - 1
                    val location = h.locations.getJSONArray(page)
                    val poly = PolygonF(location.length())
                    for (i in 0 until location.length()) {
                        val point = location.getJSONArray(i)
                        val x = (point.getString(0)).toFloat()
                        val y = (point.getString(1)).toFloat()
                        poly.addPoint(x, y)
                    }
                    pageLocations.append(intPage, poly)
                }
            }

            return PublicationHotspotV2 (
                offer = h.offer?.let { HotspotOfferV2.fromDecodable(it) },
                pageLocations = pageLocations
            )
        }
    }

    fun normalize(width: Double, height: Double) {
        val polygons = ArrayList<PolygonF>()
        for (p in getPages()) {
            polygons.add(pageLocations.get(p))
        }
        for (p in polygons) {
            for (i in 0 until p.npoints) {
                p.ypoints[i] = p.ypoints[i] / height.toFloat()
                p.xpoints[i] = p.xpoints[i] / width.toFloat()
            }
        }
    }

    private fun getPages(): IntArray {
        val pages = IntArray(pageLocations.size())
        for (i in 0 until pageLocations.size()) {
            pages[i] = pageLocations.keyAt(i)
        }
        return pages
    }


}

@Parcelize
data class HotspotOfferV2(
    val id: Id,
    val heading: String,
    val runDateRange: ValidityPeriod,
    val publishDate: PublishDate?,
    val price: PriceV2?,
    val quantity: QuantityV2?
): Parcelable {

    companion object {
        fun fromDecodable(h: HotspotOfferV2Decodable): HotspotOfferV2 {
            // sanity check on the dates
            val fromDate = h.runFromDateStr?.toValidityDate() ?: distantPast()
            val tillDate = h.runTillDateStr?.toValidityDate() ?: distantFuture()

            return HotspotOfferV2(
                id = h.id,
                heading = h.heading,
                runDateRange = minOf(fromDate, tillDate)..maxOf(
                    fromDate,
                    tillDate
                ),
                publishDate = h.publishDateStr?.toValidityDate(),
                price = h.price,
                quantity = h.quantity
            )
        }
    }
}
