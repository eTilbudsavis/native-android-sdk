package com.tjek.sdk.demo.publication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.tjek.sdk.demo.R
import com.tjek.sdk.demo.base.BaseActivity
import com.tjek.sdk.api.TjekAPI
import com.tjek.sdk.api.models.IncitoOffer
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.api.remote.ResponseType
import com.tjek.sdk.publicationviewer.incito.*
import com.tjek.sdk.publicationviewer.incito.IncitoPublicationFragment.Companion.newInstance
import kotlinx.coroutines.launch
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
            ) {
                Toast.makeText(this@IncitoPublicationActivity, incitoOffer.title, Toast.LENGTH_SHORT).show()
            }

            override fun onOfferLongClick(
                incitoOffer: IncitoOffer,
                publicationV2: PublicationV2?,
            ) {
                printOfferDetailsOnConsole(incitoOffer)
                Toast.makeText(this@IncitoPublicationActivity, "Offer details printed in console", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun printOfferDetailsOnConsole(incitoOffer: IncitoOffer) {
        lifecycleScope.launch {
            when (val res = TjekAPI.getOfferFromIncito(incitoOffer)) {
                is ResponseType.Error -> Log.e(TAG, res.toString())
                is ResponseType.Success -> Log.d(TAG, res.data.toString())
            }
        }
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