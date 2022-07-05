package com.tjek.sdk.api.models

import android.graphics.RectF
import android.os.Parcelable
import android.util.SparseArray
import com.tjek.sdk.api.*
import com.tjek.sdk.api.remote.models.v2.HotspotOfferV2Decodable
import com.tjek.sdk.api.remote.models.v2.PriceV2
import com.tjek.sdk.api.remote.models.v2.PublicationHotspotV2Decodable
import com.tjek.sdk.api.remote.models.v2.QuantityV2
import com.tjek.sdk.publicationviewer.paged.utils.PolygonF
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import kotlin.math.abs

private const val significantArea: Double = 0.02

@Parcelize
data class PublicationHotspotV2(
    val offer: HotspotOfferV2?,
    val pageLocations: SparseArray<PolygonF> = SparseArray()
): Parcelable {

    companion object {
        fun fromDecodable(h: PublicationHotspotV2Decodable): PublicationHotspotV2 {
            val pageLocations = SparseArray<PolygonF>()
            val json = JSONObject(h.locations?.utf8() ?: "")
            json.keys().let {
                while (it.hasNext()) {
                    val page = it.next()
                    val intPage = Integer.valueOf(page) - 1
                    val location = json.getJSONArray(page)
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

    fun getBoundsForPages(pages: IntArray): RectF? {
        var rect: RectF? = null
        val pagesLength = pages.size.toFloat()
        val pageOffset = 1f / pagesLength
        for (i in pages.indices) {
            val page = pages[i]
            val p: PolygonF? = pageLocations.get(page)
            p?.let{
                val r = RectF(p.bounds)
                r.right = r.right / pagesLength
                r.left = r.left / pagesLength
                r.offset(pageOffset * i.toFloat(), 0f)
                if (rect == null) {
                    rect = r
                } else {
                    rect!!.union(r)
                }
            }
        }
        return rect
    }

    fun hasLocationAt(visiblePages: IntArray, clickedPage: Int, x: Float, y: Float): Boolean {
        val p: PolygonF? = pageLocations.get(clickedPage)
        return p != null && p.contains(x, y) && isAreaSignificant(visiblePages, clickedPage)
    }

    private fun isAreaSignificant(visiblePages: IntArray, clickedPage: Int): Boolean {
        return !(visiblePages.size == 1 && pageLocations.size() > 1) || getArea(clickedPage) > significantArea
    }

    private fun getArea(page: Int): Double {
        val p: PolygonF? = pageLocations.get(page)
        return if (p == null) 0.0 else (abs(p.bounds.height()) * abs(p.bounds.width())).toDouble()
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
