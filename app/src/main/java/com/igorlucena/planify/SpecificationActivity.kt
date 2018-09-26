package com.igorlucena.planify

import android.annotation.TargetApi
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
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.lang.Math.round
import java.net.URL
import java.util.*

class SpecificationActivity : AppCompatActivity() {

    var API_URL = "https://es.wikipedia.org/w/api.php?action=query&format=json&prop=revisions&rvprop=content&titles="
    val API_VISION_KEY = "AIzaSyDK0sjfsIqaOEQyNygIjSgr3aIh9hVlpX4"
    var mSpecificationsHtml = ""
    var mHtmlText = ""
    var mTitlePlane = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specification)

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
                title_plane.text = mTitlePlane

                catchWikipedia(mTitlePlane.replace(" ", "%20"))
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
        val maxSpeedName = "Velocidad de crucero máxima"
        var maxSpeedValue = mSpecificationsHtml.subSequence(
                mSpecificationsHtml.findAnyOf(listOf(maxSpeedName))!!.first,
                mSpecificationsHtml.lastIndex)
        maxSpeedValue = maxSpeedValue.subSequence(
                maxSpeedValue.indexOf('(') + 1,
                maxSpeedValue.indexOf(')'))
                .toString()

        maxSpeedValue = maxSpeedValue.replace('\\', ' ')


        max_speed_txt.text = ": $maxSpeedValue"
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
