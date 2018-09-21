package com.igorlucena.planify

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.ProxyFileDescriptorCallback
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.Html.FROM_HTML_OPTION_USE_CSS_COLORS
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.Button
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {

    var API_URL = "https://es.wikipedia.org/w/api.php?action=query&format=json&prop=revisions&rvprop=content&titles="
    val REQUEST_IMAGE_CAPTURE = 1
    val HTML = "html"
    val TITLE_PLANE = "title_plane"
    var mModelPlane = ""
    var specificationIntent = Intent()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val photoButton = findViewById<Button>(R.id.photo_button)

        specificationIntent = Intent(this, SpecificationActivity::class.java)

        photoButton.setOnClickListener {
            dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val extras = data!!.extras

            //catchVision()
            mModelPlane = "Boeing 777"

            specificationIntent.putExtras(extras)
            specificationIntent.putExtra(TITLE_PLANE, mModelPlane)
            // Put the brand of the plane provided from VISION API Google
            catchWikipedia("Boeing%20777")
        }
    }

    private fun catchVision() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun catchWikipedia(airplaneBrand: String) {
        doAsync {
            API_URL += airplaneBrand
            val response = URL(API_URL).readText()

            val result = JSONObject(response)
                    .getJSONObject("query")
                    .getJSONObject("pages")
                    .getJSONObject("145100")["revisions"]
                    .toString()

            val htmlCode = Html.fromHtml(result.subSequence(result.indexOf('*')+4,
                    result.lastIndex-2).toString(), FROM_HTML_OPTION_USE_CSS_COLORS)

            uiThread {
                specificationIntent.putExtra(HTML, htmlCode)

                startActivity(specificationIntent)
            }
        }
    }
}
