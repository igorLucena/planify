package com.igorlucena.planify

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.text.Html
import android.view.View
import kotlinx.android.synthetic.main.activity_especification.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.net.URL

class SpecificationActivity : AppCompatActivity() {

    var API_URL = "https://es.wikipedia.org/w/api.php?action=query&format=json&prop=revisions&rvprop=content&titles="
    var specificationsHtml = ""
    var htmlText = ""
    var titlePlane = ""

    @TargetApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_especification)

        indeterminateBar.visibility = View.VISIBLE
        layout_specification.visibility = View.GONE

        val extra = intent.extras
        image_plane.setImageBitmap(extra!!.get("data") as Bitmap)

        titlePlane = "Boeing 777"

        title_plane.text = titlePlane

        catchWikipedia("Boeing%20777")
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
                    result.lastIndex-2).toString(), Html.FROM_HTML_OPTION_USE_CSS_COLORS)

            uiThread {
                indeterminateBar.visibility = View.GONE
                layout_specification.visibility = View.VISIBLE
                htmlText = htmlCode.toString()
                setSpecificationsHtml()
                setWingspanValue()
                setCruisingSpeed()
                setMaxSpeed()
                setLength()
                setSpectrum()
                setDescription()
                setFirstFlight()
            }
        }
    }

    private fun setFirstFlight() {
        val firstFlightName = "primer vuelo"
        var firstFlightValue = htmlText.subSequence(
                htmlText.findAnyOf(listOf(firstFlightName))!!.first,
                htmlText.lastIndex)
                .toString()

        val i1 = firstFlightValue.findAnyOf(listOf("[["))!!.first+2
        val i2 = firstFlightValue.findAnyOf(listOf("\n"))!!.first
        firstFlightValue = firstFlightValue.subSequence(
                firstFlightValue.findAnyOf(listOf("[["))!!.first+2,
                firstFlightValue.findAnyOf(listOf("\n"))!!.first)
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
        var descriptionValue = htmlText.subSequence(
                htmlText.findAnyOf(listOf(descriptionName))!!.first+1,
                htmlText.lastIndex)
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
        var spectrumValue = specificationsHtml.subSequence(
                specificationsHtml.findAnyOf(listOf(spectrumName))!!.first,
                specificationsHtml.lastIndex)
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
        var lengthValue = specificationsHtml.subSequence(
                specificationsHtml.findAnyOf(listOf(lengthName))!!.first,
                specificationsHtml.lastIndex)
        lengthValue = lengthValue.subSequence(
                lengthValue.indexOf(',') - 2,
                lengthValue.indexOf(',') + 2)
                .toString()

        length_txt.text = ": $lengthValue m"
    }

    private fun setMaxSpeed() {
        val maxSpeedName = "Velocidad de crucero máxima"
        var maxSpeedValue = specificationsHtml.subSequence(
                specificationsHtml.findAnyOf(listOf(maxSpeedName))!!.first,
                specificationsHtml.lastIndex)
        maxSpeedValue = maxSpeedValue.subSequence(
                maxSpeedValue.indexOf('(') + 1,
                maxSpeedValue.indexOf(')'))
                .toString()

        maxSpeedValue = maxSpeedValue.replace('\\', ' ')


        max_speed_txt.text = ": $maxSpeedValue"
    }

    private fun setCruisingSpeed() {
        val cruisingSpeedName = "Velocidad de crucero normal"
        var cruisingSpeedValue = specificationsHtml.subSequence(
                specificationsHtml.findAnyOf(listOf(cruisingSpeedName))!!.first,
                specificationsHtml.lastIndex)
        cruisingSpeedValue = cruisingSpeedValue.subSequence(
                cruisingSpeedValue.indexOf('(') + 1,
                cruisingSpeedValue.indexOf(')'))
                .toString()

        cruisingSpeedValue = cruisingSpeedValue.replace('[', ' ')
        cruisingSpeedValue = cruisingSpeedValue.replace(']', ' ')
        cruisingSpeedValue = cruisingSpeedValue.replace('\\', ' ')


        cruising_speed_txt.text = ": $cruisingSpeedValue"
    }

    private fun setSpecificationsHtml() {
        val specificationName = resources.getString(R.string.specifications)

        specificationsHtml = htmlText.subSequence(
                htmlText.findAnyOf(listOf(specificationName))!!.first,
                htmlText.lastIndex)
                .toString()

        specificationsHtml = specificationsHtml.subSequence(
                0,
                specificationsHtml.findAnyOf(listOf("Fuentes"))!!.first - 1)
                .toString()
    }

    private fun setWingspanValue() {
        val wingspanName = resources.getString(R.string.wingspan)
        var wingspanValue = specificationsHtml.subSequence(
                specificationsHtml.findAnyOf(listOf(wingspanName))!!.first,
                specificationsHtml.lastIndex)
        wingspanValue = wingspanValue.subSequence(
                wingspanValue.indexOf('m') - 5,
                wingspanValue.indexOf('m') + 1)


        wingspan_txt.text = ": $wingspanValue"
    }
}
