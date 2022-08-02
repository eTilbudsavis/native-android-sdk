package com.tjek.sdk.publicationviewer.incito

import android.net.Uri
import com.shopgun.android.sdk.log.SgnLog
import com.tjek.sdk.TjekLogCat
import com.tjek.sdk.api.models.IncitoOffer
import com.tjek.sdk.api.models.IncitoViewId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.util.*

private const val TAG = "IncitoParser"

//This dispatcher is optimized to perform CPU-intensive work outside of the main thread.
suspend fun parseIncitoJson(json: String): Map<IncitoViewId, IncitoOffer>? = withContext(Dispatchers.Default) {
        TjekLogCat.v("$TAG: convert string to JSON.....")

        val initialView = try {
            val document = JSONObject(json)
            document.getJSONObject("root_view")
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }

        initialView?.let {
            TjekLogCat.v("$TAG running......")

            val offersMap: MutableMap<String, IncitoOffer> = mutableMapOf()

            // "rootView" is the initial viewId
            getOffersId(offersMap, initialView)

            TjekLogCat.v("$TAG finished")

            // return the map
            offersMap
        }
    }


/*
  For every view, it iterates recursively on all child views and checks if the view is an offer.
  in case it is, it'll save the viewId and the metadata.
*/
private fun getOffersId(offersMap: MutableMap<String, IncitoOffer>, view: JSONObject) {
    try {
        // not all views have an id, so in case it's missing it'll be the parent id
        val viewId = view.optString("id")

        // examine all child views
        if (view.has("child_views")) {
            val childViews = view.getJSONArray("child_views")
            for (i in 0 until childViews.length()) {
                getOffersId(offersMap, childViews.getJSONObject(i))
            }
        }

        // check the role only for views with id
        if (viewId.isNotBlank() && view.has("role") && view.getString("role") == "offer") {
            val tjekOffer = view.getJSONObject("meta").getJSONObject("tjek.offer.v1")
            val title = tjekOffer.optString("title")
            var description: String? = ""
            var link: String? = ""
            val list: MutableList<String> = ArrayList()
            if (tjekOffer.has("description")) {
                description = tjekOffer.optString("description")
            }
            if (tjekOffer.has("link")) {
                link = tjekOffer.optString("link")
            }

            // get feature labels
            // THIS IS ON THE VIEW OBJ, NOT IN THE META
            if (view.has("feature_labels")) {
                val fl = view.getJSONArray("feature_labels")
                for (i in 0 until fl.length()) {
                    list.add(fl.getString(i))
                }
            }

            // add the incito offer to the map
            offersMap[viewId] = IncitoOffer(
                viewId = viewId,
                title = title,
                description = description,
                link = Uri.parse(link),
                featureLabels = list)
        }
    } catch (e: JSONException) {
        e.printStackTrace()
        TjekLogCat.d("$TAG -> parse error")
    }
}