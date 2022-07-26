package com.shopgun.android.sdk.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.lifecycle.lifecycleScope
import com.shopgun.android.sdk.demo.base.BaseActivity
import com.tjek.sdk.api.TjekAPI
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.api.remote.LocationQuery
import com.tjek.sdk.api.remote.ResponseType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PublicationListActivity : BaseActivity() {

    private var publications: List<PublicationV2> = ArrayList()
    private val listAdapter = PublicationAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publication_list_layout)
        val listView: ListView = findViewById<View>(R.id.list) as ListView
        listView.adapter = listAdapter

        showProgress("", "Loading publications....")

        lifecycleScope.launch(Dispatchers.Main) {
            val res = TjekAPI.getPublications(
                nearLocation = LocationQuery(Constants.TJEK_HQ)
            )
            if (res is ResponseType.Success) {
                publications = res.data?.results!!
                listAdapter.notifyDataSetChanged()
                hideProgress()
            }
        }
    }

    inner class PublicationAdapter : BaseAdapter() {

        override fun getCount(): Int {
            return publications.size
        }

        override fun getItem(position: Int): Any {
            return publications[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView
                ?: LayoutInflater.from(this@PublicationListActivity)
                    .inflate(R.layout.card_publication, parent, false)
            val tv = view.findViewById<TextView>(R.id.card_dealer)
            val logo = view.findViewById<ImageView>(R.id.card_logo)
            val label = view.findViewById<TextView>(R.id.card_label)
            val pdf = view.findViewById<Button>(R.id.card_pdf)
            val incito = view.findViewById<Button>(R.id.card_incito)
            val p = publications[position]
            tv.text = p.branding.name
            label.text = p.label ?: ""
            pdf.visibility = if (p.hasPagedPublication) View.VISIBLE else View.GONE
            incito.visibility = if (p.hasIncitoPublication) View.VISIBLE else View.GONE
            GlideApp.with(applicationContext)
                .load(p.frontPageImages.thumb)
                .fitCenter()
                .placeholder(R.drawable.placeholder_px)
                .into(logo)

            pdf.setOnClickListener { PagedPublicationActivity.start(this@PublicationListActivity, p) }
            incito.setOnClickListener { IncitoPublicationActivity.start(this@PublicationListActivity, p) }
            return view
        }
    }
}