package com.shopgun.android.sdk.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import com.shopgun.android.sdk.demo.base.BaseActivity
import com.tjek.sdk.api.models.IncitoOffer
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.publicationviewer.incito.*
import com.tjek.sdk.publicationviewer.incito.IncitoPublicationFragment.Companion.newInstance
import kotlin.math.roundToInt

class IncitoPublicationActivity : BaseActivity() {

    private var incitoFragment: IncitoPublicationFragment? = null
    private lateinit var progress: ProgressBar

    private var publicationConfiguration = IncitoPublicationConfiguration()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incito_publication_layout)

        progress = findViewById(R.id.incito_progress)

        incitoFragment =
            supportFragmentManager.findFragmentByTag(FRAGMENT_TAG) as IncitoPublicationFragment?

        if (incitoFragment == null) {
            val publication: PublicationV2 = intent.extras!!.getParcelable(KEY_PUB)!!
            incitoFragment = newInstance(publication, publicationConfiguration)
            supportFragmentManager
                .beginTransaction()
                .add(R.id.incitoPublication, incitoFragment!!, FRAGMENT_TAG)
                .commit()
        }
        addIncitoListener()
    }

    private fun addIncitoListener() {
        incitoFragment?.setIncitoEventListener(object : IncitoEventListener {
            override fun onProgressChanged(
                progress: Float,
                verticalScrollOffset: Int,
            ) {
                this@IncitoPublicationActivity.progress.progress = (progress * 100).roundToInt()
            }

            override fun onOfferClick(
                incitoOffer: IncitoOffer,
                publicationV2: PublicationV2?,
            ) {// todo onOfferClick
            }

            override fun onOfferLongClick(
                incitoOffer: IncitoOffer,
                publicationV2: PublicationV2?,
            ) {
            }

        })
    }

    companion object {
        val TAG = IncitoPublicationActivity::class.java.simpleName
        const val FRAGMENT_TAG = "publication-fragment"
        private const val KEY_PUB = "PUBLICATION"
        fun start(context: Context, publication: PublicationV2?) {
            val i = Intent(context, IncitoPublicationActivity::class.java)
            i.putExtra(KEY_PUB, publication)
            context.startActivity(i)
        }
    }
}