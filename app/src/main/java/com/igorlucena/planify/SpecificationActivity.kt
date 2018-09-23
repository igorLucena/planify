package com.igorlucena.planify

import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.igorlucena.planify.R.string.*
import kotlinx.android.synthetic.main.activity_especification.*

class SpecificationActivity : AppCompatActivity() {

    var specificationsHtml = ""
    var htmlText = ""
    var titlePlane = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_especification)

        val extra = intent.extras
        htmlText = (extra!!.get("html") as CharSequence).toString()
        titlePlane = (extra!!.get("title_plane") as CharSequence).toString()
        image_plane.setImageBitmap(extra!!.get("data") as Bitmap)

        title_plane.text = titlePlane

        catchSpecificationHtml()
        setWingspanValue()
        setCruisingSpeed()
        setMaxSpeed()
        setLength()
        setSpectrum()
        setDescription()
        setFirstFlight()
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
        val lengthName = resources.getString(length)
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

    private fun catchSpecificationHtml() {
        val specificationName = resources.getString(specifications)

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
        val wingspanName = resources.getString(wingspan)
        var wingspanValue = specificationsHtml.subSequence(
                specificationsHtml.findAnyOf(listOf(wingspanName))!!.first,
                specificationsHtml.lastIndex)
        wingspanValue = wingspanValue.subSequence(
                wingspanValue.indexOf('m') - 5,
                wingspanValue.indexOf('m') + 1)


        wingspan_txt.text = ": $wingspanValue"
    }
}
