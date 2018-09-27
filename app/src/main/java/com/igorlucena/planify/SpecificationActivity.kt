package com.igorlucena.planify

import android.annotation.TargetApi
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.text.Html
import android.view.View
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionRequestInitializer
import com.google.api.services.vision.v1.model.*
import kotlinx.android.synthetic.main.activity_specification.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.lang.Math.round
import java.net.URL
import java.time.ZoneId
import java.util.*

class SpecificationActivity : AppCompatActivity() {

    var API_URL = "https://es.wikipedia.org/w/api.php?action=query&format=json&prop=revisions&rvprop=content&titles="
    val API_VISION_KEY = "AIzaSyDK0sjfsIqaOEQyNygIjSgr3aIh9hVlpX4"
    val RESTRICTIONS_VISION_API = "restrictions"
    var mSpecificationsHtml = ""
    var mHtmlText = ""
    var mTitlePlane = ""
    var mRestrictions = 0
    var mSharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specification)

        mSharedPreferences = getSharedPreferences(RESTRICTIONS_VISION_API,
                Context.MODE_PRIVATE)
        mRestrictions = mSharedPreferences!!.getInt("9", 0)

        indeterminateBar.visibility = View.VISIBLE

        layout_specification.visibility = View.GONE

        val extra = intent.extras
        image_plane.setImageBitmap(extra!!.get("data") as Bitmap)

        catchVision()
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun catchVision() {
        val vision = Vision.Builder(NetHttpTransport(), AndroidJsonFactory(), null)
                .setVisionRequestInitializer(VisionRequestInitializer(API_VISION_KEY))
                .build()

        val inputStream = resources.openRawResource(R.raw.air4)
        val photoData = org.apache.commons.io.IOUtils.toByteArray(inputStream)
        inputStream.close()

        val inputImage = Image()
        inputImage.encodeContent(photoData)

        val desiredFeature = Feature()
        desiredFeature.setType("LABEL_DETECTION")

        val request = AnnotateImageRequest()
        request.setImage(inputImage)
        request.setFeatures(Arrays.asList(desiredFeature))

        val batchRequest = BatchAnnotateImagesRequest()
        batchRequest.setRequests(Arrays.asList(request))

        doAsync {
            val batchResponse = vision.images().annotate(batchRequest).execute()
            mRestrictions++
            val editor = mSharedPreferences!!.edit()
            val date = Date()
            val month = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                localDate.monthValue
            } else {
                val cal = Calendar.getInstance()
                cal.time = date
                cal.get(Calendar.MONTH)
            }
            editor.putInt(month.toString(), mRestrictions)
            editor.commit()

            val descriptions = batchResponse.responses.get(0).labelAnnotations

            var airplane = false
            for (i in (0..(descriptions.size - 1))) {
                var description = descriptions.get(i).description
                if (description.equals("airplane")) {
                    airplane = true
                }

                if (airplane && description.startsWith("boeing ") ||
                        description.startsWith("airbus "))
                {
                    val descriptionList = description.split(Regex.fromLiteral(" "))
                    description = ""
                    for (str in descriptionList) {
                        description += str.capitalize()
                        description += ' '
                    }
                    mTitlePlane = description.trim()
                }
            }

            uiThread {
                if (!airplane) {
                    val message = resources.getString(R.string.no_exists_airplane)
                    startActivity(intentFor<ErrorActivity>("error" to message))
                    finish()
                } else if (mTitlePlane.length == 0) {
                    val message = resources.getString(R.string.no_specification)
                    startActivity(intentFor<ErrorActivity>("error" to message))
                    finish()
                } else {
                    title_plane.text = mTitlePlane

                    catchWikipedia(mTitlePlane.replace(" ", "%20"))
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun catchWikipedia(airplaneBrand: String) {
        doAsync {
            API_URL += airplaneBrand
            val response = URL(API_URL).readText()

            val number = JSONObject(response)
                    .getJSONObject("query")
                    .getJSONObject("pages")
                    .keys()
                    .next()

            val result = JSONObject(response)
                    .getJSONObject("query")
                    .getJSONObject("pages")
                    .getJSONObject(number)["revisions"]
                    .toString()

            val htmlCode = Html.fromHtml(result.subSequence(result.indexOf('*')+4,
                    result.lastIndex-2).toString(), Html.FROM_HTML_OPTION_USE_CSS_COLORS)
            mHtmlText = htmlCode.toString()
            setSpecificationsHtml()

            uiThread {
                setWingspanValue()
                setCruisingSpeed()
                setMaxSpeed()
                setLength()
                setSpectrum()
                setDescription()
                setFirstFlight()
                indeterminateBar.visibility = View.GONE
                layout_specification.visibility = View.VISIBLE
                toast("Ha utilizado $mRestrictions de 10 solicitudes en la aplicación.")
            }
        }
    }

    private fun setFirstFlight() {
        val firstFlightName = "primer vuelo"
        var firstFlightValue = mHtmlText.subSequence(
                mHtmlText.findAnyOf(listOf(firstFlightName))!!.first,
                mHtmlText.lastIndex)
                .toString()

        firstFlightValue = firstFlightValue.replace(']', ' ')
        firstFlightValue = firstFlightValue.subSequence(
                0,
                firstFlightValue.findAnyOf(listOf("[["))!!.first+7)
                .toString()
        firstFlightValue = firstFlightValue.replace('[', ' ')




        first_flight_txt.text = ": $firstFlightValue"
    }

    private fun setDescription() {
        val descriptionName = "nEl"
        var descriptionValue = mHtmlText.subSequence(
                mHtmlText.findAnyOf(listOf(descriptionName))!!.first+1,
                mHtmlText.lastIndex)
                .toString()

        descriptionValue = descriptionValue.subSequence(
                0,
                descriptionValue.indexOf('{')
        ).toString()

        descriptionValue = descriptionValue.replace('[', ' ')
        descriptionValue = descriptionValue.replace(']', ' ')

        description_txt.text = "$descriptionValue"
    }

    private fun setSpectrum() {
        val spectrumName = "Alcance"
        var spectrumValue = mSpecificationsHtml.subSequence(
                mSpecificationsHtml.findAnyOf(listOf(spectrumName))!!.first,
                mSpecificationsHtml.lastIndex)
        spectrumValue = spectrumValue.subSequence(
                spectrumValue.findAnyOf(listOf("n|"))!!.first + 3,
                spectrumValue.findAnyOf(listOf("||"))!!.first)
                .toString()

        spectrumValue = spectrumValue.replace('[', ' ')
        spectrumValue = spectrumValue.replace(']', ' ')

        spectrum_txt.text = ": $spectrumValue"
    }

    private fun setLength() {
        val lengthName = resources.getString(R.string.length)
        var lengthValue = mSpecificationsHtml.subSequence(
                mSpecificationsHtml.findAnyOf(listOf(lengthName))!!.first,
                mSpecificationsHtml.lastIndex)
        lengthValue = lengthValue.subSequence(
                lengthValue.indexOf(',') - 2,
                lengthValue.indexOf(',') + 2)
                .toString()

        length_txt.text = ": $lengthValue m"
    }

    private fun setMaxSpeed() {
        val maxSpeedName1 = "Velocidad de crucero máxima"
        val maxSpeedName2 = "Máxima velocidad de crucero"
        var maxSpeedValue = ""

        if (mSpecificationsHtml.findAnyOf(listOf(maxSpeedName1)) == null) {
            maxSpeedValue = mSpecificationsHtml.subSequence(
                    mSpecificationsHtml.findAnyOf(listOf(maxSpeedName2))!!.first,
                    mSpecificationsHtml.lastIndex)
                    .toString()
        } else {
            maxSpeedValue = mSpecificationsHtml.subSequence(
                    mSpecificationsHtml.findAnyOf(listOf(maxSpeedName1))!!.first,
                    mSpecificationsHtml.lastIndex)
                    .toString()
        }

        if (maxSpeedValue.length == 0) {
            max_speed_txt.text = ": -"
        } else {
            maxSpeedValue = maxSpeedValue.subSequence(
                    maxSpeedValue.indexOf(',') - 1,
                    maxSpeedValue.indexOf(',') + 3)
                    .toString()

            val maxSpeedValueExp = round(maxSpeedValue.toString().replace(',','.').toFloat() * 1235.0)

            max_speed_txt.text = ": $maxSpeedValueExp km/h"
        }
    }

    private fun setCruisingSpeed() {
        val cruisingSpeedName = "Velocidad de crucero"
        var cruisingSpeedValue = mSpecificationsHtml.subSequence(
                mSpecificationsHtml.findAnyOf(listOf(cruisingSpeedName))!!.first,
                mSpecificationsHtml.lastIndex)
        cruisingSpeedValue = cruisingSpeedValue.subSequence(
                cruisingSpeedValue.indexOf(',') - 1,
                cruisingSpeedValue.indexOf(',') + 3)

        val cruisingSpeedValueExp = round(cruisingSpeedValue.toString().replace(',','.').toFloat() * 1235.0)


        cruising_speed_txt.text = ": $cruisingSpeedValueExp km/h"
    }

    private fun setSpecificationsHtml() {
        val specificationName = resources.getString(R.string.specifications)

        mSpecificationsHtml = mHtmlText.subSequence(
                mHtmlText.findAnyOf(listOf(specificationName))!!.first,
                mHtmlText.lastIndex)
                .toString()

        mSpecificationsHtml = mSpecificationsHtml.subSequence(
                0,
                mSpecificationsHtml.findAnyOf(listOf("Referencias"))!!.first - 1)
                .toString()
    }

    private fun setWingspanValue() {
        val wingspanName = resources.getString(R.string.wingspan)
        var wingspanValue = mSpecificationsHtml.subSequence(
                mSpecificationsHtml.findAnyOf(listOf(wingspanName))!!.first,
                mSpecificationsHtml.lastIndex)
        wingspanValue = wingspanValue.subSequence(
                wingspanValue.indexOf('m') - 5,
                wingspanValue.indexOf('m') + 1)


        wingspan_txt.text = ": $wingspanValue"
    }
}
